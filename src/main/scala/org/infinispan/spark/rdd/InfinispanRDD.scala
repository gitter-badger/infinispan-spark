package org.infinispan.spark.rdd

import java.net.InetSocketAddress
import java.util.Properties

import org.apache.spark.rdd.RDD
import org.apache.spark.{Partition, SparkContext, TaskContext}
import org.infinispan.client.hotrod.{Flag, RemoteCacheManager}
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.spark._

import scala.collection.JavaConversions._

/**
 * @author gustavonalle
 */
class InfinispanRDD[K, V](@transient val sc: SparkContext,
                          @transient val splitter: Splitter = new PerServerSplitter,
                          val configuration: Properties)
        extends RDD[(K, V)](sc, Nil) {

   override def compute(split: Partition, context: TaskContext): Iterator[(K, V)] = {
      val infinispanPartition = split.asInstanceOf[InfinispanPartition]
      val config = infinispanPartition.properties
      val cacheName = config.getProperty(InfinispanRDD.CacheName)
      val batch = config.read[Integer](InfinispanRDD.BatchSize)(default = InfinispanRDD.DefaultBatchSize)
      val address = infinispanPartition.location.address.asInstanceOf[InetSocketAddress]
      val host = address.getHostString
      val port = address.getPort
      val builder = new ConfigurationBuilder().withProperties(configuration).addServer().host(host).port(port)
      val remoteCacheManager = new RemoteCacheManager(builder.build())
      val cache = Option(cacheName).map(name => remoteCacheManager.getCache(name)).getOrElse(remoteCacheManager.getCache)
      val segmentFilter = infinispanPartition.segments.map(setAsJavaSet).orNull
      val closeableIterator = cache.retrieveEntries(null, segmentFilter, batch)
      context addTaskCompletionListener (t => {
         closeableIterator.close()
         remoteCacheManager.stop()
      })
      new InfinispanIterator(closeableIterator, context)
   }

   override protected def getPreferredLocations(split: Partition): Seq[String] =
      Seq(split.asInstanceOf[InfinispanPartition].location.address.asInstanceOf[InetSocketAddress].getHostString)

   override protected def getPartitions: Array[Partition] = {
      val remoteCacheManager = new RemoteCacheManager(new ConfigurationBuilder().withProperties(configuration).pingOnStartup(true).build())
      val optCacheName = Option(configuration.getProperty(InfinispanRDD.CacheName))
      val cache = optCacheName.map(remoteCacheManager.getCache(_)).getOrElse(remoteCacheManager.getCache())
      val segmentsByServer = cache.getCacheTopologyInfo.getSegmentsPerServer
      splitter.split(segmentsByServer.toMap.mapValues(_.toSet), configuration)
   }
}

object InfinispanRDD {
   val DefaultBatchSize = 10000
   val DefaultPartitionsPerServer = 2

   val CacheName = "infinispan.rdd.cacheName"
   val BatchSize = "infinispan.rdd.batch_size"
   val PartitionsPerServer = "infinispan.rdd.number_server_partitions"
   val FilterFactory = "infinispan.rdd.filter_factory"
}

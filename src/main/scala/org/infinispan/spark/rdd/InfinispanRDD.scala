package org.infinispan.spark.rdd

import java.net.InetSocketAddress

import org.apache.spark.rdd.RDD
import org.apache.spark.{Partition, SparkContext, TaskContext}
import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder

import scala.collection.JavaConversions._

/**
 * @author gustavonalle
 * @since 8.0
 *
 */
class InfinispanRDD[K, V](@transient val sc: SparkContext,
                          @transient val splitter: Splitter = new PerServerSplitter)
        extends RDD[(K, V)](sc, Nil) {

   private val DefaultBatch = 1000

   override def compute(split: Partition, context: TaskContext): Iterator[(K, V)] = {
      val infinispanPartition = split.asInstanceOf[InfinispanPartition]
      val location = infinispanPartition.location
      val host = location.address.asInstanceOf[InetSocketAddress].getHostString
      val port = location.address.asInstanceOf[InetSocketAddress].getPort
      val cacheName = infinispanPartition.cacheName
      val conf = new ConfigurationBuilder().addServer().host(host).port(port.toInt).pingOnStartup(true).build()
      val remoteCacheManager = new RemoteCacheManager(conf)
      val cache = remoteCacheManager.getCache(cacheName)
      val segmentFilter = infinispanPartition.segments.map(setAsJavaSet).orNull
      val batch = infinispanPartition.batch
      val closeableIterator = cache.retrieveEntries(null, segmentFilter, batch)
      new InfinispanIterator(closeableIterator, context)
   }

   override protected def getPreferredLocations(split: Partition): Seq[String] =
      Seq(split.asInstanceOf[InfinispanPartition].location.address.asInstanceOf[InetSocketAddress].getHostString)

   override protected def getPartitions: Array[Partition] = {
      val host = sc.getConf.get("infinispan.server.host")
      val port = sc.getConf.get("infinispan.server.port")
      val optCacheName = sc.getConf.getOption("infinispan.cache.name")
      val optBatch = sc.getConf.getOption("infinispan.cache.batch")
      val conf = new ConfigurationBuilder().addServer().host(host).port(port.toInt).pingOnStartup(true).build()
      val remoteCacheManager = new RemoteCacheManager(conf)
      val cache = optCacheName.map(name => remoteCacheManager.getCache(name)).getOrElse(remoteCacheManager.getCache)
      val segmentsByServer = cache.getSegmentsByServer
      splitter.split(segmentsByServer.toMap.mapValues(_.toSet), cache.getName, optBatch.map(_.toInt).getOrElse(DefaultBatch))
   }
}

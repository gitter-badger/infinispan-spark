package org.infinispan.spark.rdd

import java.util.Properties

import org.apache.spark.TaskContext
import org.apache.spark.rdd.RDD
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.client.hotrod.{RemoteCache, RemoteCacheManager}

import scala.collection.JavaConversions._


/**
 * Infinispan specific functions
 *
 * @author gustavonalle
 */
class InfinispanRDDFunctions[K, V](rdd: RDD[(K, V)]) extends Serializable {

   def writeToInfinispan(configuration: Properties): Unit = {
      val processor = (ctx: TaskContext, iterator: Iterator[(K, V)]) => {
         new InfinispanWriteJob(configuration).runJob(iterator, ctx)
      }
      rdd.sparkContext.runJob(rdd, processor)
   }

   private class InfinispanWriteJob(val configuration: Properties) extends Serializable {
      private def getCacheManager: RemoteCacheManager = {
         val builder = new ConfigurationBuilder().withProperties(configuration)
         new RemoteCacheManager(builder.build())
      }

      def runJob(iterator: Iterator[(K, V)], ctx: TaskContext): Unit = {
         val remoteCacheManager = getCacheManager
         ctx.addTaskCompletionListener { f => remoteCacheManager.stop() }
         val cacheName = configuration.getProperty(InfinispanRDD.CacheName)
         val cache: RemoteCache[K, V] = Option(cacheName).map(name => remoteCacheManager.getCache(name)).getOrElse(remoteCacheManager.getCache).asInstanceOf[RemoteCache[K, V]]
         val map = iterator.toMap
         cache.putAll(map)
      }

   }

}

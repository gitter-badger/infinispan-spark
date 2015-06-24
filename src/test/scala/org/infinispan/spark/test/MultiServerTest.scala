package org.infinispan.spark.test

import org.infinispan.client.hotrod.test.MultiHotRodServersTest
import org.infinispan.configuration.cache.{CacheMode, ConfigurationBuilder}
import org.infinispan.test.AbstractCacheTest

/**
 * @author gustavonalle
 * @since 8.0
 */
abstract class MultiServerTest /*extends MultiHotRodServersTest with Spark*/ {

//   val cacheMode: CacheMode
//
//   val numServers: Int = 3
//   val numEntries: Int = 10000
//   lazy val remoteCache = client(0).getCache
//
//   override def getPort = server(0).getPort
//
//   override def createCacheManagers() = createHotRodServers(numServers, getCacheConfiguration)
//
//   protected def getCacheConfiguration: ConfigurationBuilder = {
//      val builder: ConfigurationBuilder = AbstractCacheTest.getDefaultClusteredCacheConfig(cacheMode, false)
//      builder.clustering.hash.numOwners(2)
//      builder
//   }

}

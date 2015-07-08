package org.infinispan.spark

import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.spark.test.{CacheType, Cluster}
import org.scalatest.{BeforeAndAfterAll, Suites}

import scala.concurrent.duration._
import scala.util.Try

/**
 * Aggregates all suites that requires a running cluster.
 */
class ClusteredSuite extends Suites(new DistributedSuite, new ReplicatedSuite) with BeforeAndAfterAll {
   override protected def beforeAll(): Unit = {
      ClusteredServers.startServers()
      super.beforeAll()
   }

   override protected def afterAll(): Unit = {
      ClusteredServers.stopServers()
      super.afterAll()
   }
}

object ClusteredServers {
   private var cluster: Cluster = _

   Runtime.getRuntime.addShutdownHook(new Thread {
      override def run(): Unit = Try(cluster.stop())
   })

   def startServers() = {
      cluster = new Cluster(size = 2, location = getClass.getResource("/infinispan-server/").getPath)
      cluster.startAndWait(60 seconds)
      cluster.createCache("replicated", CacheType.REPLICATED)
   }

   def stopServers() = {
      cluster.stop()
   }

   def getServers = cluster.getServers.toList

   def getRemoteCacheManager: RemoteCacheManager = new RemoteCacheManager(new ConfigurationBuilder().addServer().host("127.0.0.1").port(getServers.head.getHotRodPort).pingOnStartup(true).build)
}

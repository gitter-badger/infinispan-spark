package org.infinispan.spark

import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.spark.test.InfinispanServer
import org.scalatest.{BeforeAndAfterAll, Suites}

import scala.util.Try

/**
 * Aggregates all suites that requires a single non clustered server.
 */
class NonClusteredSuites extends Suites(new SQLSuite, new NonClusteredSuite, new StreamingSuite, new WriteSuite) with BeforeAndAfterAll {

   override protected def beforeAll(): Unit = {
      SingleNode.start()
      super.beforeAll()
   }

   override protected def afterAll(): Unit = {
      SingleNode.stop()
      super.afterAll()
   }
}

object SingleNode {

   protected var server: InfinispanServer = _

   Runtime.getRuntime.addShutdownHook(new Thread {
      override def run(): Unit = Try(server.stop())
   })

   def start() = {
      server = new InfinispanServer(getClass.getResource("/infinispan-server/").getPath, "server1")
      server.start()
      server.waitForLocalCacheManager()
   }

   def stop() = {
      server.stop()
   }

   def getServerPort = server.getHotRodPort

   def getRemoteCacheManager: RemoteCacheManager = new RemoteCacheManager(new ConfigurationBuilder().addServer().host("127.0.0.1").port(server.getHotRodPort).pingOnStartup(true).build)
}


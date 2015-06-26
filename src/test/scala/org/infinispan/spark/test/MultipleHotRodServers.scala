package org.infinispan.spark.test

import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.client.hotrod.test.HotRodClientTestingUtil
import org.infinispan.client.hotrod.test.HotRodClientTestingUtil._
import org.infinispan.configuration.cache.ConfigurationBuilder
import org.infinispan.lifecycle.ComponentStatus
import org.infinispan.server.hotrod.HotRodServer
import org.infinispan.test.TestingUtil
import org.infinispan.test.TestingUtil._
import org.infinispan.test.fwk.{TestCacheManagerFactory, TransportFlags}
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.collection.mutable.ListBuffer


trait
MultipleHotRodServers extends BeforeAndAfterAll with RemoteTest {
   this: Suite =>

   protected def numServers: Int

   protected def getConfigurationBuilder: ConfigurationBuilder

   protected val servers: ListBuffer[HotRodServer] = ListBuffer()

   protected val remoteCacheManagers: ListBuffer[RemoteCacheManager] = ListBuffer()

   override protected def beforeAll(): Unit = {
      val hotRodServers = (1 to numServers).map { _ =>
         val clusteredCacheManager = TestCacheManagerFactory.createClusteredCacheManager(getConfigurationBuilder, new TransportFlags)
         startHotRodServer(clusteredCacheManager)
      }
      val cache = hotRodServers.head.getCacheManager.getCache()
      blockUntilViewReceived(cache, numServers)
      hotRodServers.foreach(s => blockUntilCacheStatusAchieved(s.getCacheManager.getCache(), ComponentStatus.RUNNING, 1000L))
      val rcms = hotRodServers.map { s =>
         val clientBuilder = new org.infinispan.client.hotrod.configuration.ConfigurationBuilder()
         clientBuilder.addServer().host("localhost").port(s.getPort).pingOnStartup(false)
         new RemoteCacheManager(clientBuilder.build());
      }
      servers ++= hotRodServers
      remoteCacheManagers ++= rcms

      super.beforeAll()
   }

   override protected def afterAll(): Unit = {
      HotRodClientTestingUtil.killServers(servers: _*)
      TestingUtil.killCacheManagers(servers.map(_.getCacheManager): _*)
      super.afterAll()
   }

   protected def getRemoteCacheManagers: List[RemoteCacheManager] = remoteCacheManagers.toList

   protected def pickCacheManager: RemoteCacheManager = remoteCacheManagers.head

   protected def getServers: List[HotRodServer] = servers.toList

   protected def pickServer: HotRodServer = servers.head

}

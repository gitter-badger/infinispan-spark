package org.infinispan.spark.test

import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.client.hotrod.test.HotRodClientTestingUtil
import org.infinispan.client.hotrod.test.HotRodClientTestingUtil._
import org.infinispan.manager.EmbeddedCacheManager
import org.infinispan.server.hotrod.HotRodServer
import org.infinispan.server.hotrod.test.HotRodTestingUtil._
import org.infinispan.test.TestingUtil
import org.infinispan.test.fwk.TestCacheManagerFactory
import org.scalatest.{BeforeAndAfterAll, Suite}

trait SingleHotRodServer extends BeforeAndAfterAll with RemoteTest {
   this: Suite =>

   protected var hotRotServer: HotRodServer = _
   private var remoteCacheManager: RemoteCacheManager = _
   private var cacheManager: EmbeddedCacheManager = _

   override protected def beforeAll(): Unit = {
      this.hotRotServer = createHotRodServer
      this.remoteCacheManager = createRemoteCacheManager
      this.cacheManager = TestCacheManagerFactory.createCacheManager(hotRodCacheConfiguration())
      super.beforeAll()
   }

   protected def createRemoteCacheManager = new RemoteCacheManager(new ConfigurationBuilder().addServer().host("127.0.0.1").port(hotRotServer.getPort).build)

   protected def createHotRodServer = HotRodClientTestingUtil.startHotRodServer(createCacheManager)

   protected def createCacheManager = TestCacheManagerFactory.createCacheManager(hotRodCacheConfiguration())

   override protected def afterAll(): Unit = {
      killRemoteCacheManager(remoteCacheManager)
      killServers(hotRotServer)
      TestingUtil.killCacheManagers(cacheManager)
      super.afterAll()
   }

   protected def pickCacheManager: RemoteCacheManager = remoteCacheManager

   protected def pickServer: HotRodServer = hotRotServer

   protected def getRemoteCacheManagers: List[RemoteCacheManager] = List(remoteCacheManager)

   protected def getServers: List[HotRodServer] = List(hotRotServer)
}



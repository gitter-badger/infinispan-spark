package org.infinispan.spark.test

import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.server.hotrod.HotRodServer

trait RemoteTest {

   protected def getRemoteCacheManagers: List[RemoteCacheManager]

   protected def pickCacheManager: RemoteCacheManager

   protected def getServers: List[HotRodServer]

   protected def pickServer: HotRodServer
}

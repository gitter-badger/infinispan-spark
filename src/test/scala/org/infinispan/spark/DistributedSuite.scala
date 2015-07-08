package org.infinispan.spark

import org.infinispan.client.hotrod.RemoteCache
import org.infinispan.spark.test.{RemoteTest, Spark, WordCache}
import org.scalatest.{DoNotDiscover, Matchers}

@DoNotDiscover
class DistributedSuite extends RDDRetrievalTest with WordCache with Spark with RemoteTest with Matchers {
   override protected def getNumEntries: Int = 100

   override def getCache[K, V]: RemoteCache[K, V] = ClusteredServers.getRemoteCacheManager.getCache.asInstanceOf[RemoteCache[K, V]]

   override def getServerPort: Int = ClusteredServers.getServers.head.getHotRodPort
}


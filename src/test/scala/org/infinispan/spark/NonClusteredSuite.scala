package org.infinispan.spark

import org.infinispan.client.hotrod.RemoteCache
import org.infinispan.spark.test._
import org.scalatest.DoNotDiscover

@DoNotDiscover
class NonClusteredSuite extends RDDRetrievalTest with WordCache with Spark with RemoteTest {
   override def getNumEntries: Int = 100

   override def getCache[K, V]: RemoteCache[K, V] = SingleNode.getRemoteCacheManager.getCache.asInstanceOf[RemoteCache[K, V]]

   override def getServerPort: Int = SingleNode.getServerPort
}
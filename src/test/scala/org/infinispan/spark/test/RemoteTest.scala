package org.infinispan.spark.test

import org.infinispan.client.hotrod.RemoteCache
import org.infinispan.spark.SingleNode

/**
 * Trait to be mixed-in by tests that require a one or more running infinispan server.
 *
 * @author gustavonalle
 */
trait RemoteTest {
   def getCache[K, V]: RemoteCache[K, V] = SingleNode.getRemoteCacheManager.getCache.asInstanceOf[RemoteCache[K, V]]

   def getServerPort: Int = SingleNode.getServerPort
}

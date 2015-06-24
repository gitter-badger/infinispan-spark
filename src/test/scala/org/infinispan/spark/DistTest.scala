package org.infinispan.spark

import org.infinispan.client.hotrod.RemoteCache
import org.infinispan.configuration.cache.CacheMode
import org.infinispan.spark.test.MultiServerTest
import org.testng.annotations.{BeforeClass, Test}

/**
 * @author gustavonalle
 * @since 8.0
 */
//@Test(groups = Array("functional"), testName = "spark.DistTest")
class DistTest /*extends MultiServerTest with RDDRetrievalTest*/ {
//   override val cacheMode: CacheMode = CacheMode.DIST_SYNC
//
//   lazy val cache = remoteCache.asInstanceOf[RemoteCache[Int,String]]
//
//   @BeforeClass(alwaysRun = true)
//   override def before() = {
//      super.before()
//      (1 to numEntries).foreach(i => cache.put(i, s"val$i"))
//   }

}

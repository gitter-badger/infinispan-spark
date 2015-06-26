package org.infinispan.spark

import org.infinispan.commons.equivalence.AnyServerEquivalence
import org.infinispan.configuration.cache.CacheMode
import org.infinispan.spark.test._
import org.infinispan.test.AbstractCacheTest._
import org.scalatest.Matchers

class ReplicatedSuite extends RDDRetrievalTest with WordCache with Spark with MultipleHotRodServers with Matchers {
   override protected def numServers: Int = 3

   override protected def getNumEntries: Int = 10000

   override protected def getConfigurationBuilder = {
      val builder = getDefaultClusteredCacheConfig(CacheMode.DIST_SYNC, false)
      builder.dataContainer().keyEquivalence(new AnyServerEquivalence).valueEquivalence(new AnyServerEquivalence)
      builder
   }
}

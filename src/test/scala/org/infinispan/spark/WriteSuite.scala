package org.infinispan.spark

import java.util.Properties

import org.infinispan.client.hotrod.impl.ConfigurationProperties
import org.infinispan.commons.equivalence.AnyServerEquivalence
import org.infinispan.configuration.cache.{CacheMode, ConfigurationBuilder}
import org.infinispan.spark.domain.Runner
import org.infinispan.spark.rdd._
import org.infinispan.spark.test.{MultipleHotRodServers, Spark}
import org.infinispan.test.AbstractCacheTest._
import org.scalatest.{FunSuite, Matchers}


class WriteSuite extends FunSuite with Spark with MultipleHotRodServers with Matchers {

   override protected def getConfigurationBuilder: ConfigurationBuilder = {
      val builder = getDefaultClusteredCacheConfig(CacheMode.DIST_SYNC, false)
      builder.dataContainer().keyEquivalence(new AnyServerEquivalence).valueEquivalence(new AnyServerEquivalence)
      builder
   }

   override protected def numServers: Int = 4


   test("write to infinispan") {
      val cm = pickCacheManager
      val entities = (for (num <- 0 to 999) yield new Runner(s"name$num", true, num * 10, (1000 - 30) / 50)).toSeq

      val rdd = sc.parallelize(entities).zipWithIndex().map { case (a, b) => (b, a) }

      val props = new Properties()
      props.put(ConfigurationProperties.SERVER_LIST, s"localhost:${pickServer.getPort}")

      val cache = cm.getCache[Long, Runner]()

      cache.size shouldBe 0

      rdd.writeToInfinispan(props)

      cache.size() shouldBe 1000
      cache.get(350L).name shouldBe "name350"

   }

}

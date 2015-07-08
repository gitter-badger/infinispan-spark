package org.infinispan.spark

import java.util.Properties

import org.infinispan.client.hotrod.RemoteCache
import org.infinispan.spark.domain.Runner
import org.infinispan.spark.test.{RemoteTest, Spark}
import org.scalatest.{DoNotDiscover, FunSuite, Matchers}

@DoNotDiscover
class WriteSuite extends FunSuite with Spark with RemoteTest with Matchers {

   test("write to infinispan") {
      val entities = (for (num <- 0 to 999) yield new Runner(s"name$num", true, num * 10, (1000 - 30) / 50)).toSeq

      val rdd = sc.parallelize(entities).zipWithIndex().map {
         _.swap
      }

      val props = new Properties()
      props.put("infinispan.client.hotrod.server_list", s"localhost:$getServerPort")

      val cache = getCache.asInstanceOf[RemoteCache[Int, Runner]]
      cache.clear()

      rdd.writeToInfinispan(props)

      cache.size() shouldBe 1000
      cache.get(350L).name shouldBe "name350"
   }
}

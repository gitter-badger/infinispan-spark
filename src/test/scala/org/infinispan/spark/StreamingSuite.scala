package org.infinispan.spark

import java.util.Properties

import org.apache.spark.streaming._
import org.infinispan.client.hotrod.RemoteCache
import org.infinispan.client.hotrod.impl.ConfigurationProperties
import org.infinispan.spark.stream._
import org.infinispan.spark.test.StreamingUtils.TestInputDStream
import org.infinispan.spark.test.{SingleHotRodServer, Spark}
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.duration._

/**
 * @author gustavonalle
 */
class StreamingSuite extends FunSuite with Spark with SingleHotRodServer with Matchers {

   test("test write from stream") {
      val cache = pickCacheManager.getCache.asInstanceOf[RemoteCache[Int, String]]

      val ssc = new StreamingContext(sc, Seconds(1))

      val stream = new TestInputDStream(ssc, of = Seq(1 -> "value1", 2 -> "value2", 3 -> "value3"), streamItemEvery = 100 millis)

      val props = new Properties()
      props.put(ConfigurationProperties.SERVER_LIST, s"localhost:${pickServer.getPort}")

      stream.writeToInfinispan(props)

      ssc.start()

      ssc.awaitTerminationOrTimeout(2000)

      cache.size shouldBe 3
      cache.get(1) shouldBe "value1"
      cache.get(2) shouldBe "value2"
      cache.get(3) shouldBe "value3"

      ssc.stop(true, true)
   }

}

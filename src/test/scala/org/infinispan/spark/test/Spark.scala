package org.infinispan.spark.test

import java.util.Properties

import org.apache.spark.{SparkConf, SparkContext}
import org.infinispan.spark.rdd.InfinispanRDD
import org.scalatest.{BeforeAndAfterAll, Suite}

/**
 * Trait to be mixed-in by tests that require a [[org.apache.spark.SparkContext]]
 *
 * @author gustavonalle
 */
trait Spark extends BeforeAndAfterAll {
   this: Suite with RemoteTest =>

   private lazy val config: SparkConf = new SparkConf().setMaster("local[8]").setAppName(this.getClass.getName)
   protected var sc: SparkContext = _

   def createInfinispanRDD[K, V] = {
      val properties = new Properties()
      val port = getServerPort
      properties.put("infinispan.client.hotrod.server_list", Seq("localhost", port).mkString(":"))
      properties.put("infinispan.rdd.cacheName", getCache.getName)
      new InfinispanRDD[K, V](sc, configuration = properties)
   }

   override protected def beforeAll(): Unit = {
      sc = new SparkContext(config)
      super.beforeAll()
   }

   override protected def afterAll(): Unit = {
      sc.stop()
      super.afterAll()
   }

}

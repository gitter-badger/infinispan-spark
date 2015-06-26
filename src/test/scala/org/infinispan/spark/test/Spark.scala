package org.infinispan.spark.test

import java.util.Properties

import org.apache.spark.{SparkConf, SparkContext}
import org.infinispan.client.hotrod.impl.ConfigurationProperties
import org.infinispan.spark.rdd.InfinispanRDD
import org.scalatest.{BeforeAndAfterAll, Suite}


trait Spark extends BeforeAndAfterAll {
   this: Suite with RemoteTest =>

   private lazy val config: SparkConf = new SparkConf().setMaster("local[4]").setAppName(this.getClass.getName)
   protected var sc: SparkContext = _

   def createInfinispanRDD[K, V] = {
      val properties = new Properties()
      properties.put(ConfigurationProperties.SERVER_LIST, Seq("localhost", pickServer.getPort).mkString(":"))
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

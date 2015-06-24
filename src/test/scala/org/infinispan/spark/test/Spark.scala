package org.infinispan.spark.test

import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.{BeforeAndAfterAll, Suite}


trait Spark extends BeforeAndAfterAll {
   this: Suite with RemoteTest =>

   protected var sc: SparkContext = _

   private lazy val config: SparkConf = new SparkConf()
           .setMaster("local[4]")
           .set("infinispan.server.host", getHost)
           .set("infinispan.server.port", pickServer.getPort.toString)
           .setAppName(this.getClass.getName)

   override protected def beforeAll(): Unit = {
      sc = new SparkContext(config)
      super.beforeAll()
   }

   override protected def afterAll(): Unit = {
      sc.stop()
      super.afterAll()
   }

   def getHost: String = "localhost"

}

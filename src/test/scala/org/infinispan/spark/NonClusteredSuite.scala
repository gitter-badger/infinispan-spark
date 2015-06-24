package org.infinispan.spark

import org.infinispan.spark.test._

class NonClusteredSuite extends RDDRetrievalTest with WordCache with Spark with SingleHotRodServer {
   override def getNumEntries: Int = 10000
}
package org.infinispan.spark

import org.infinispan.spark.rdd.InfinispanRDD
import org.infinispan.spark.test.{Spark, WordCache}
import org.scalatest.{FunSuite, Matchers}

abstract class RDDRetrievalTest extends FunSuite with Matchers {
   self: WordCache with Spark =>

   test("RDD Operators") {
      val infinispanRDD = new InfinispanRDD[Integer, String](sc)
      // Count
      infinispanRDD.count() shouldBe getNumEntries

      // Sort By Key
      val first = infinispanRDD.sortByKey().first()
      first._1 shouldBe 1

      // Count by Key
      val map = infinispanRDD.countByKey()
      map.forall { case (k, v) => v == 1 } shouldBe true

      // Filter
      val halfSize = getNumEntries / 2
      val filteredRDD = infinispanRDD.filter { case (k, _) => k > halfSize }
      halfSize shouldBe filteredRDD.count()

      //      // Collect and Sort
      //      val arrayOfTuple = infinispanRDD.collect().sorted
      //      assertEquals(numEntries, arrayOfTuple.length)
      //      assertEquals((1, "val1"), arrayOfTuple.head)
      //      assertEquals((numEntries, s"val$numEntries"), arrayOfTuple(numEntries - 1))
      //
      //      // Max/Min
      //      val maxEntry = infinispanRDD.max()
      //      val minEntry = infinispanRDD.min()
      //      assertEquals((1, "val1"), minEntry)
      //      assertEquals((numEntries, s"val$numEntries"), maxEntry)
      //
      //      // RDD combination operations
      //      val data = Array(1, 2, 3, 4, 5)
      //      val memoryRDD = sc.parallelize(data)
      //
      //      val cartesianRDD = memoryRDD.cartesian(infinispanRDD)
      //      assertEquals(numEntries * data.length, cartesianRDD.count())
      //
      //      val otherRDD = sc.makeRDD((1 to 5).map(Integer.valueOf).map(e => e -> s"val$e").toSeq)
      //      val subtractedRDD = infinispanRDD.subtract(otherRDD, numPartitions = 2)
      //      assertEquals(numEntries - otherRDD.count(), subtractedRDD.count())
      //
      //      // Map Reduce
      //      val sum = infinispanRDD.map { case (key, value) => key }.reduce(_ + _)
      //      assertEquals((numEntries + 1) * numEntries / 2, sum)

   }

}

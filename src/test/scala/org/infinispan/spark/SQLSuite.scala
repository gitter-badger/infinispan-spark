package org.infinispan.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.infinispan.spark.domain.Runner
import org.infinispan.spark.rdd.InfinispanRDD
import org.infinispan.spark.test.{RunnersCache, SingleHotRodServer, Spark}
import org.scalatest.{FunSuite, Matchers}

class SQLSuite extends FunSuite with RunnersCache with Spark with SingleHotRodServer with Matchers {

   override def getNumEntries: Int = 10000

   test("SQL Group By") {
      withSqlContext { (sqlContext, runnersRDD) =>
         val winners = sqlContext.sql(
            """
              |SELECT MIN(r.finishTimeSeconds) as time, first(r.name) as name, first(r.age) as age
              |FROM runners r WHERE
              |r.finished = true GROUP BY r.age
              |
            """.stripMargin).collect()

         /* Check winners */
         winners.foreach { row =>
            val winnerTime = row.getAs[Int]("time")
            val age = row.getAs[Int]("age")
            val fasterOfAge = runnersRDD.filter(r => r.age == age && r.finished).sortBy(_.finishTimeSeconds).first()
            fasterOfAge.finishTimeSeconds shouldBe winnerTime
         }
      }
   }

   test("SQL Count") {
      withSqlContext { (sqlContext, _) =>
         val count = sqlContext.sql("SELECT count(*) AS result from runners").collect().head.getAs[Long]("result")
         count shouldBe getNumEntries
      }
   }

   private def withSqlContext(f: (SQLContext, RDD[Runner]) => Any) = {
      val runnersRDD = new InfinispanRDD[Integer, Runner](sc).values
      val sqlContext = new SQLContext(sc)
      val dataFrame = sqlContext.createDataFrame(runnersRDD)
      dataFrame.registerTempTable("runners")
      f(sqlContext, runnersRDD)
   }
}

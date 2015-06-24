package org.infinispan.spark

//@Test(groups = Array("functional"), testName = "spark.DistWithEntitiesTest")
class DistWithEntitiesTest /*extends MultiServerTest*/ {

   //   val NumEntries = 20000
   //   override val cacheMode: CacheMode = CacheMode.DIST_SYNC
   //   lazy val cache = remoteCache.asInstanceOf[RemoteCache[Int, LogEntry]]
   //
   //   @BeforeClass(alwaysRun = true)
   //   override def before() = {
   //      super.before()
   //      val entries = EntryGenerator.generate(
   //         NumEntries,
   //         errorCondition = _.userAgent == "MSIE",
   //         startDate = LocalDate.of(2015, 6, 10),
   //         endDate = LocalDate.of(2015, 6, 19)
   //      )
   //      entries.zipWithIndex.map { case (e, idx) => cache.put(idx, e) }
   //   }
   //
   //   @Test
   //   def testEntities(): Unit = {
   //      val infinispanRDD = new InfinispanRDD[Integer, LogEntry](sc)
   //
   //      infinispanRDD.persist()
   //
   //      /* Count */
   //      assertEquals(NumEntries, infinispanRDD.count())
   //
   //      /* Find log entries with errors on Sundays */
   //      val errors = infinispanRDD.filter { case (k, v) =>
   //         v.date.getDayOfWeek == DayOfWeek.SUNDAY && v.opCode == 500
   //      }.values
   //
   //      errors.foreach { log =>
   //         assertTrue(log.opCode == 500)
   //         assertTrue(log.userAgent == "MSIE")
   //      }
   //
   //      /**
   //       * Calculate browser usage report on a daily basis:
   //       * 2015/2/14 -> Map(MSIE -> 2, Opera -> 3, ...)
   //       * 2015/2/15 -> Map(MSIE -> 0, Opera -> 1, ...)
   //       */
   //      val report = infinispanRDD.map {
   //         case (k, v) => (v.date, v.userAgent)
   //      }.aggregateByKey(collection.mutable.Map[String, Int]())((a, b) => {
   //         a.put(b, a.getOrElse(b, 0) + 1)
   //         a
   //      }, (a, b) => {
   //         a ++ b.map { case (browser, count) => browser -> (count + a.getOrElse(browser, 0)) }
   //      }).collectAsMap()
   //
   //      /* Check report correctness */
   //      report.foreach { case (date, map) =>
   //         map.foreach { case (browser, count) =>
   //            val count = infinispanRDD.filter { case (k, v) => v.userAgent == browser && v.date == date }.count()
   //            assertEquals(count, report(date)(browser))
   //         }
   //      }
   //   }
}

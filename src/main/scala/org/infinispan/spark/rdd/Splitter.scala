package org.infinispan.spark.rdd

import java.net.SocketAddress

import org.apache.spark.Partition

/**
 * @author gustavonalle
 */
trait Splitter {

   def split(segmentsByServer: Map[SocketAddress, Set[Integer]], cacheName: String, batch: Int): Array[Partition]
}

/**
 * Splits in one partition per server, trying to distribute equally the segments among each server
 */
class PerServerSplitter extends Splitter {

   override def split(segmentsByServer: Map[SocketAddress, Set[Integer]], cacheName: String, batch: Int): Array[Partition] = {
      if (segmentsByServer.isEmpty) throw new IllegalArgumentException("No servers found to partition")
      if (segmentsByServer.keys.size == 1 && segmentsByServer.values.flatten.isEmpty) {
         Array(new SingleServerPartition(segmentsByServer.keySet.head, cacheName, batch))
      } else {
         val numServers = segmentsByServer.keySet.size
         val numSegments = segmentsByServer.values.flatten.max + 1
         val segmentsPerServer = Math.ceil(numSegments.toFloat / numServers.toFloat).toInt

         val taken = collection.mutable.Set[Integer]()
         val available = collection.mutable.Set[Integer]()
         segmentsByServer.zipWithIndex.map {
            case ((server, segments), index) =>
               val split = collection.mutable.Set[Integer]()
               val leftFromPrevious = segments.intersect(available).diff(taken).take(segmentsPerServer)
               val notTaken = segments.diff(taken)
               split ++= leftFromPrevious
               split ++= notTaken.take(segmentsPerServer - split.size)
               taken ++= split
               available ++= notTaken
               new InfinispanPartition(index, Location(server), Some(split.toSet), cacheName, batch)
         }.toArray
      }
   }

}

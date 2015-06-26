package org.infinispan.spark.rdd

import java.net.SocketAddress

import org.apache.spark.Partition

/**
 * @author gustavonalle
 */
class Location(val address: SocketAddress) extends Serializable

object Location {
   def apply(a: SocketAddress) = new Location(a)
}

class InfinispanPartition(val idx: Int, val location: Location, val segments: Option[Set[Integer]], val cacheName: String, val batch: Int) extends Partition {
   override def index: Int = idx
}

class SingleServerPartition(server: SocketAddress, cacheName: String, batch: Int) extends InfinispanPartition(0, Location(server), None, cacheName, batch)

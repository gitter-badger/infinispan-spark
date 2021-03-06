package org.infinispan.spark.stream

import java.nio._
import java.util.Properties

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.receiver.Receiver
import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.client.hotrod.annotation.{ClientCacheEntryCreated, ClientCacheEntryModified, ClientCacheEntryRemoved, ClientListener}
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.client.hotrod.event.{ClientCacheEntryCustomEvent, ClientEvent}
import org.infinispan.commons.io.UnsignedNumeric
import org.infinispan.spark.rdd.InfinispanRDD

/**
 * @author gustavonalle
 */
class InfinispanInputDStream[K, V](@transient ssc_ : StreamingContext, storageLevel: StorageLevel, configuration: Properties) extends ReceiverInputDStream[(K, V, ClientEvent.Type)](ssc_) {
   override def getReceiver(): Receiver[(K, V, ClientEvent.Type)] = new EventsReceiver(storageLevel, configuration)
}

private class EventsReceiver[K, V](storageLevel: StorageLevel, configuration: Properties) extends Receiver[(K, V, ClientEvent.Type)](storageLevel) {

   @transient private lazy val remoteCache = {
      val rcm = new RemoteCacheManager(new ConfigurationBuilder().withProperties(configuration).pingOnStartup(true).build())
      val optCacheName = Option(configuration.getProperty(InfinispanRDD.CacheName))
      optCacheName.map(rcm.getCache(_)).getOrElse(rcm.getCache())
   }
   @transient private lazy val listener = new EventListener

   override def onStart(): Unit = remoteCache.addClientListener(listener)

   override def onStop(): Unit = remoteCache.removeClientListener(listener)

   @ClientListener(converterFactoryName = "___eager-key-value-version-converter", useRawData = true)
   private class EventListener {

      @ClientCacheEntryRemoved
      def onRemove(event: ClientCacheEntryCustomEvent[Array[Byte]]) {
         val marshaller = remoteCache.getRemoteCacheManager.getMarshaller
         val eventData = event.getEventData
         val rawData = ByteBuffer.wrap(eventData)
         val rawKey = readElement(rawData)
         val key: K = marshaller.objectFromByteBuffer(rawKey).asInstanceOf[K]
         store((key, null.asInstanceOf[V], event.getType))
      }

      @ClientCacheEntryCreated
      @ClientCacheEntryModified
      def onAddModify(event: ClientCacheEntryCustomEvent[Array[Byte]]) = {
         val marshaller = remoteCache.getRemoteCacheManager.getMarshaller
         val eventData = event.getEventData
         val rawData = ByteBuffer.wrap(eventData)
         val rawKey = readElement(rawData)
         val rawValue = readElement(rawData)
         val key: K = marshaller.objectFromByteBuffer(rawKey).asInstanceOf[K]
         val value: V = marshaller.objectFromByteBuffer(rawValue).asInstanceOf[V]
         store((key, value, event.getType))
      }

      private def readElement(in: ByteBuffer): Array[Byte] = {
         val length = UnsignedNumeric.readUnsignedInt(in)
         val element = new Array[Byte](length)
         in.get(element)
         element
      }
   }

}







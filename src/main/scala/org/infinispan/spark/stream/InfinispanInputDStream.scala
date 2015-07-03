package org.infinispan.spark.stream

import java.util.Properties

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.receiver.Receiver
import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.client.hotrod.annotation.{ClientCacheEntryCreated, ClientCacheEntryModified, ClientCacheEntryRemoved, ClientListener}
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.client.hotrod.event.{ClientCacheEntryCustomEvent, ClientEvent}
import org.infinispan.commons.util.KeyValueWithPrevious
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

   @ClientListener(converterFactoryName = "key-value-with-previous-converter-factory")
   private class EventListener {

      @ClientCacheEntryCreated
      @ClientCacheEntryModified
      @ClientCacheEntryRemoved
      def onEvent(event: ClientCacheEntryCustomEvent[KeyValueWithPrevious[K, V]]) = {
         val eventData = event.getEventData
         val key: K = eventData.getKey
         val value: V = eventData.getValue
         store((key, value, event.getType))
      }
   }

}







package org.infinispan.spark.test

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.receiver.Receiver

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

/**
 * @author gustavonalle
 */
object StreamingUtils {

   class TestReceiver[T](of: Seq[T], streamItemEvery: Duration) extends Receiver[T](StorageLevel.MEMORY_ONLY) {
      override def onStart(): Unit = {
         of.foreach { item =>
            Thread.sleep(streamItemEvery.toMillis)
            store(item)
         }
      }

      override def onStop(): Unit = {}
   }

   class TestInputDStream[T: ClassTag](@transient ssc_ : StreamingContext, of: Seq[T], streamItemEvery: Duration) extends ReceiverInputDStream[T](ssc_) {
      override def getReceiver(): Receiver[T] = new TestReceiver[T](of, streamItemEvery)
   }

}

package org.infinispan.spark.rdd

import java.util.Properties

class EnhancedProperties(props: Properties) {

   def read[T](key: String)(default: T) = Option(props.get(key)).map(_.asInstanceOf[T]).getOrElse(default)
}

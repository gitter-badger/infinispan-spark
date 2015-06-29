package org.infinispan.spark

import java.util.Properties

import org.apache.spark.rdd.RDD

package object rdd {

   implicit def props(p: Properties): EnhancedProperties = new EnhancedProperties(p)

   implicit def infinispanCustomFunctions[K, V](rdd: RDD[(K, V)]): InfinispanRDDFunctions[K, V] = new InfinispanRDDFunctions[K, V](rdd)

}

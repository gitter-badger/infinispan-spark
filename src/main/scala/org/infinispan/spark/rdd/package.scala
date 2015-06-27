package org.infinispan.spark

import java.util.Properties

package object rdd {

   implicit def props(p: Properties): EnhancedProperties = new EnhancedProperties(p)

}

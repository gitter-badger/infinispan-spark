package org.infinispan.spark.domain

/**
 * @author gustavonalle
 */
case class Tweet(id: Long, user: String, country: String, favourite: Int, followers: Int, text: String)

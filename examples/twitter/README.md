## Twitter Demo

This sample shows the integration between Infinispan and Spark including RDD operations, writing from RDDs, Spark SQL 
and Streaming.

### 1. Preparing the environment

The ```gustavonalle/infinispan-spark``` image will be used to hold both infinispan and spark cluster.

#### Launching the master

``` docker run --name master -ti gustavonalle/infinispan-spark ```

Obtain the master ip by running:

```docker inspect master | grep IPAddress ```

#### Launching workers

Launch one or more workers with:

``` docker run -ti --link master:master  gustavonalle/infinispan-spark ```

#### Check the console

Point your browser at ```http://master-ip:9080```, check if all workers are listed.

### 2. Starting the shell

Start the shell passing the master IP address as a parameter:

```
./run-shell.sh master-ip
```

### 3. Running the sample

#### Importing packages

Copy and paste the import blocks in the shell:


```scala
import twitter4j._
import org.apache.log4j._
import java.util._

import org.apache.spark._
import org.apache.spark.storage._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.StreamingContext._ 

import org.infinispan.client.hotrod._
import org.infinispan.client.hotrod.impl.ConfigurationProperties._
import org.infinispan.client.hotrod.configuration._
import org.infinispan.spark.domain._
import org.infinispan.spark.rdd._
import org.infinispan.spark.stream._

import scala.collection.JavaConversions._

```

#### Reducing log level

To avoid extremely verbose output, let's hack the log level. Paste the line in the shell:

```scala
Logger.getRootLogger.setLevel(Level.WARN)
```


#### Adding twitter4j authorization

Since Spark twitter connector requires authentication, please fill the following system variables with your credentials
and paste them in the shell.
The credentials can be obtained by any twitter account holder by following instructions on 
https://dev.twitter.com/oauth/overview/application-owner-access-tokens

```scala
System.setProperty("twitter4j.oauth.consumerKey", "...")
System.setProperty("twitter4j.oauth.consumerSecret", "....")
System.setProperty("twitter4j.oauth.accessToken", "....")
System.setProperty("twitter4j.oauth.accessTokenSecret", "....")
```

#### Creating Infinispan remote cache

Tweets will be saved to Infinispan, keyed by Id. The object that will hold the tweet is: 

```scala
case class Tweet(id: Long, user: String, country: String, retweet: Long, followers: Int, text: String)

```

Let's create a RemoteCache that will hold the data. Paste the following:

```scala
val master = sc.getConf.get("spark.master").replace("spark://","").replaceAll(":.*","")

val config = new Properties
config.put(SERVER_LIST,s"$master:11222")

val cache = new RemoteCacheManager(new ConfigurationBuilder().addServer().host(master).build).getCache.asInstanceOf[RemoteCache[Long,Tweet]]

```

#### Creating a stream of tweets

The code to create a stream out of twitter API uses Spark built-in twitter support. Paste the following:

```scala
val ssc = new StreamingContext(sc, Seconds(1))
val stream = TwitterUtils.createStream(ssc,None)

```

#### Saving the twitter transformed stream to Infinispan

We only want a subset of tweet information for this sample, so we will apply a transformation in the DStream above. 
Paste this line:

```scala
val kv = stream.map( s => (s.getId, new Tweet(s.getId, s.getUser.getScreenName, Option(s.getPlace).map(_.getCountry).getOrElse("N/A"),s.getRetweetCount, s.getUser.getFollowersCount, s.getText)) )
kv.writeToInfinispan(config)
```

The kv stream maps from ```twitter4j.Status``` object to our ```org.infinispan.spark.domain.Tweet``` and save the Stream to Infinispan

Note: so far, nothing is really happening in Infinispan and Spark, we've just declared how to start the DStream (from twitter API)
and how it will be transformed (by mapping to a different object)


#### Start everything!

In order to effectively put in motion the dataflow, we need to start the streaming context. Paste this line:

```scala
ssc.start
```

After a few seconds, tweets are going to be saved to Infinispan cache. To periodically verify the cache size, use the command:

```
cache.size
```


#### Query using spark operators

Let's query Infinispan data using some RDD operators. First create a InfinispanRDD around the cache:

```scala
val rdd = new InfinispanRDD[Long,Tweet](sc,configuration = config)
```

To count tweets:

```scala
rdd.count

```

Obtain tweets per country, showing top countries first:

```scala
rdd.collect{ case (k,v) if v.country != "N/A" => (v.country,1) }.reduceByKey(_+_).sortBy(_._2, ascending = false).take(10).foreach{ case (country,count) => println(s"$country -> $count") }

```

### Query using SparkSQL

In order to use sql, it's necessary to transform our key-value based RDD into a tabular format with a schema.
This is accomplished by letting Spark infer the schema based on the domain object and create a temporary table:


#### Schema and temp table registration

```scala
// valuesRDD has type RDD[org.infinispan.spark.domain.Tweet]
val valuesRDD = rdd.values  
// data frame has schema [id: bigint, user: string, country: string, retweet: bigint, followers: int, text: string]
val dataFrame = sqlContext.createDataFrame(valuesRDD) 
dataFrame.registerTempTable("tweets")

```

#### Running the SQL Query

To achieve the same tweets per country aggregation above, but using SQL:

```scala
sqlContext.sql("SELECT country, count(*) as c from tweets GROUP BY country ORDER BY c desc").collect().take(10).foreach(println)

```

Output:

```
[United States,442]
[Brasil,208]
[Argentina,136]
[United Kingdom,116]
[Türkiye,88]
[France,57]
[Rossiya,52]
[España,45]
[México,41]
[Portugal,31]

```


### Stream Infinispan Data

The previous queries were run against all the data in the cache. Now were going to execute
some sliding window queries, getting tweets per country in the last minute only. 
For this we are going to create a DStream that will reflect the latest cache inserts. 
Since Spark can only hold one streaming context per JVM, let's start another shell in another terminal.
Close the current running shell, and start a fresh one:

```
./run-shell.sh master-ip
```

#### Prepare the shell

We need to set-up again the imports, Twitter stream, and configs. Paste the code below, after changing 
twitter credentials:

```scala
import twitter4j._
import org.apache.log4j._
import java.util._
import org.apache.spark._
import org.apache.spark.storage._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.StreamingContext._ 
import org.infinispan.client.hotrod._
import org.infinispan.client.hotrod.impl.ConfigurationProperties._
import org.infinispan.client.hotrod.configuration._
import scala.collection.JavaConversions._
import scala.util.Random
import org.infinispan.spark.domain._
import org.infinispan.spark.rdd._
import org.infinispan.spark.stream._
import org.infinispan.spark.rdd.InfinispanRDD


Logger.getRootLogger.setLevel(Level.WARN)

System.setProperty("twitter4j.oauth.consumerKey", "....")
System.setProperty("twitter4j.oauth.consumerSecret", "....")
System.setProperty("twitter4j.oauth.accessToken", "....")
System.setProperty("twitter4j.oauth.accessTokenSecret", "....")

val master = sc.getConf.get("spark.master").replace("spark://","").replaceAll(":.*","")

val config = new Properties
config.put(SERVER_LIST,s"$master:11222")

val cache = new RemoteCacheManager(new ConfigurationBuilder().addServer().host(master).build).getCache.asInstanceOf[RemoteCache[Long,Tweet]]

val ssc = new StreamingContext(sc, Seconds(3))
val stream = TwitterUtils.createStream(ssc,None)

val kv = stream.map( s => (s.getId, new Tweet(s.getId, s.getUser.getScreenName, Option(s.getPlace).map(_.getCountry).getOrElse("N/A"),s.getRetweetCount, s.getUser.getFollowersCount, s.getText)) )
kv.writeToInfinispan(config)
```

#### Create an Infinispan DStream

To stream cache events, create an instance of ```InfinispanInputDStream```:

```scala
val infinispanStream = new InfinispanInputDStream[Long,Tweet](ssc, StorageLevel.MEMORY_ONLY, config)
```

#### Sliding query

Our infinispan stream is based on the data stored in cache (K,V) = (Long, Tweet) but we want numbers of tweets per country, so 
we apply a transformation:


```scala
// countryDStream is of type DStream[(String,Long)]
val countryDStream = infinispanStream.transform( rdd => rdd.collect{ case (k,v,_) if v.country != "N/A" => (v.country,1) }.reduceByKey(_+_))
```

Since we are interested in the last 60 seconds only, we restrict the DStream by window, collapsing all the RDDs:

```scala
val lastMinuteDStream = countryDStream.reduceByKeyAndWindow(_+_, Seconds(60))

```

Finally we print the results:


```scala
total.foreachRDD{ (rdd, time) => println(s"--------- $time -------"); rdd.sortBy(_._2).collect().foreach(println) }

```

Same as before, we need to explicitly start the processing:

```scala
ssc.start
```

Every 3 second, it should print something like:

```
--------- 1436218500000 ms -------
(United States,21)
(Brasil,14)
(Argentina,7)
(United Kingdom,4)
(France,3)
(Türkiye,3)
(México,3)
(Chile,2)
(مصر,2)
(Portugal,2)
(España,2)
(Malaysia,2)
(Indonesia,1)
(المملكة العربية السعودية,1)
(دولة قطر,1)

```
That represent the number of tweets per country, in the last 60 seconds that were inserted in the Infinispan cache.


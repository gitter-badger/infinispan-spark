#!/bin/bash

if [ $# -eq 0 ]
  then
    echo "Usage:run-shell <MASTER_IP>"
    exit 0
fi

INFINISPAN_VERSION=$(cat ../../build.sbt | grep "infinispanVersion =" | awk '{gsub(/"/,"");print $4}')

wget -P files/ -nc http://twitter4j.org/archive/twitter4j-3.0.3.zip && unzip -nd files/ files/twitter4j-3.0.3.zip
wget -P files/ -nc https://repo1.maven.org/maven2/org/apache/spark/spark-streaming-twitter_2.10/1.4.0/spark-streaming-twitter_2.10-1.4.0.jar
wget -P files/ -nc http://mirror.vorboss.net/apache/spark/spark-1.4.0/spark-1.4.0-bin-hadoop2.6.tgz && tar -xzf files/spark-1.4.0-bin-hadoop2.6.tgz -C files
wget -P files/ -nc https://repo1.maven.org/maven2/org/infinispan/infinispan-remote/$INFINISPAN_VERSION/infinispan-remote-$INFINISPAN_VERSION.jar

files/spark-1.4.0-bin-hadoop2.6/bin/spark-shell --executor-memory=1g --master spark://$1:7077 --jars files/spark-streaming-twitter_2.10-1.4.0.jar,$(ls -m files/lib/*.jar | tr -d '\n'  | tr -d ' '),../../target/scala-2.10/infinispan-spark_2.10-0.1-SNAPSHOT-tests.jar,../../target/scala-2.10/infinispan-spark_2.10-0.1-SNAPSHOT.jar,files/infinispan-remote-$INFINISPAN_VERSION.jar


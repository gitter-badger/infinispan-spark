#!/bin/bash

if [ $# -eq 0 ]
  then
    echo "Usage:run-shell <MASTER_IP>"
    exit 0
fi

wget -P files/ -nc http://twitter4j.org/archive/twitter4j-3.0.3.zip && unzip -nd files/ files/twitter4j-3.0.3.zip
wget -P files/ -nc https://repo1.maven.org/maven2/org/apache/spark/spark-streaming-twitter_2.10/1.4.0/spark-streaming-twitter_2.10-1.4.0.jar
wget -P files/ -nc http://mirror.vorboss.net/apache/spark/spark-1.4.0/spark-1.4.0-bin-hadoop2.6.tgz && tar -xzf files/spark-1.4.0-bin-hadoop2.6.tgz -C files


files/spark-1.4.0-bin-hadoop2.6/bin/spark-shell --executor-memory=1g --master spark://$1:7077 --jars files/spark-streaming-twitter_2.10-1.4.0.jar,$(ls -m files/lib/*.jar | tr -d '\n'  | tr -d ' '),infinispan-spark_2.10-0.1-SNAPSHOT-tests.jar,infinispan-spark_2.10-0.1-SNAPSHOT.jar,infinispan-remote-8.0.0-SNAPSHOT.jar


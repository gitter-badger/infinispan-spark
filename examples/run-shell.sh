#!/bin/bash

if [ $# -eq 0 ]
  then
    echo "Usage:run-shell <MASTER_IP>"
    exit 0
fi

wget -P files/ -nc http://twitter4j.org/archive/twitter4j-3.0.3.zip && unzip -nd files/ files/twitter4j-3.0.3.zip

wget -P files/ -nc https://repo1.maven.org/maven2/org/apache/spark/spark-streaming-twitter_2.10/1.4.0/spark-streaming-twitter_2.10-1.4.0.jar

# Temporarily until published 
cp $HOME/.m2/repository/org/infinispan/infinispan-remote/8.0.0-SNAPSHOT/infinispan-remote-8.0.0-SNAPSHOT.jar .


/home/gfernandes/spark/bin/spark-shell --executor-memory=1g --master spark://$1:7077 --jars files/spark-streaming-twitter_2.10-1.4.0.jar,$(ls -m files/lib/*.jar | tr -d '\n'  | tr -d ' '),../target/scala-2.10/infinispan-spark_2.10-0.1-SNAPSHOT-tests.jar,../target/scala-2.10/infinispan-spark_2.10-0.1-SNAPSHOT.jar,infinispan-remote-8.0.0-SNAPSHOT.jar

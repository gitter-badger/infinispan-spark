#!/usr/bin/env bash

VERSION=$(cat ../build.sbt| grep "infinispanVersion =" | awk '{gsub(/"/,"");print $4}')
wget -nc http://downloads.jboss.org/infinispan/$VERSION/infinispan-server-$VERSION-bin.zip
docker build -t="gustavonalle/infinispan-spark" .

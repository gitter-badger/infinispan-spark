#!/usr/bin/env bash

wget -nc http://downloads.jboss.org/infinispan/8.0.0.Beta1/infinispan-server-8.0.0.Beta1-bin.zip
docker build -t="gustavonalle/infinispan-spark" .


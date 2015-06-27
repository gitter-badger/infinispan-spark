cp /home/gfernandes/.m2/repository/org/infinispan/server/infinispan-server/8.0.0-SNAPSHOT/infinispan-server-8.0.0-SNAPSHOT-bin.zip . 
cp /home/gfernandes/code/infinispan/all/remote/target/infinispan-remote-8.0.0-SNAPSHOT.jar .
docker build -t="gustavonalle/infinispan-spark" .


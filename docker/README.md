## Docker images to test Infinispan/Spark integration

### Launching:

Launch a container containing spark master, spark worker and infinispan server:

```
docker run --name master -ti gustavonalle/infinispan-spark
``` 


Launch one or more worker container, that holds infinispan server and a spark worker:

```
docker run -ti --link master:master  gustavonalle/infinispan-spark
```

To check the master ip address, run: ```docker inspect master```

The admin UI will listen at ```http://master-ip:9080```

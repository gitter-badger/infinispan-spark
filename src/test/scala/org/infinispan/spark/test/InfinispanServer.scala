package org.infinispan.spark.test

import java.io.File
import java.net.InetAddress
import java.nio.file.Paths

import org.jboss.as.controller.PathAddress
import org.jboss.as.controller.client.ModelControllerClient
import org.jboss.as.controller.client.helpers.ClientConstants._
import org.jboss.dmr.ModelNode

import scala.annotation.tailrec
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.sys.process._
import scala.util.{Failure, Success, Try}

/**
 * @author gustavonalle
 */
object CacheType extends Enumeration {
   val LOCAL = Value("local-cache")
   val REPLICATED = Value("replicated-cache")
   val DISTRIBUTED = Value("distributed-cache")
}

/**
 * A cluster of Infinispan Servers that can be managed together
 */
class Cluster(size: Int, location: String) {
   private val _servers = mutable.ListBuffer[InfinispanServer]()

   def startAndWait(duration: Duration) = {
      val servers = for (i <- 0 to size - 1) yield {
         new InfinispanServer(location, s"server$i", clustered = true, i * 1000)
      }
      _servers ++= servers
      val futureServers = _servers.map(s => Future(s.startAndWaitForCluster(_servers.size)))
      Await.ready(Future.sequence(futureServers), duration)
   }

   def stop() = _servers.par.foreach(_.stop())

   def getFirstServer = _servers.head

   def getServers = _servers

   def createCache(name: String, cacheType: CacheType.Value) = {
      _servers.foreach(_.addCache("clustered", "replicated", CacheType.REPLICATED))
   }

}

/**
 * A remote infinispan server controlled by issuing native management operations
 */
class InfinispanServer(location: String, name: String, clustered: Boolean = false, portOffSet: Int = 0) {
   val BinFolder = "bin"
   val LaunchScript = "standalone.sh"

   val NameNodeConfig = "-Djboss.node.name"
   val LogDirConfig = "-Djboss.server.log.dir"
   val ClusteredConfig = "clustered.xml"
   val PortOffsetConfig = "-Djboss.socket.binding.port-offset"

   val Protocol = "http-remoting"
   val Host = "localhost"
   val ShutDownOp = "shutdown"

   val ManagementPort = 9990
   val HotRodPort = 11222

   private var launcher: Process = _

   def start() = {
      val logDir = Paths.get(location, "logs")
      val launch = Paths.get(location, BinFolder, LaunchScript)
      new File(launch.toString).setExecutable(true)
      val cmd = mutable.ListBuffer[String](Paths.get(location, BinFolder, LaunchScript).toString)
      if (clustered) {
         cmd += s"-c=$ClusteredConfig"
      }
      cmd += s"$NameNodeConfig=$name"
      cmd += s"$LogDirConfig=$logDir/$name"
      if (portOffSet > 0) {
         cmd += s"$PortOffsetConfig=$portOffSet"
      }
      launcher = Process(cmd).run(new ProcessLogger {
         override def out(s: => String): Unit = {}

         override def err(s: => String): Unit = {}

         override def buffer[T](f: => T): T = f
      })
   }

   def startAndWaitForCluster(size: Int) = {
      start()
      waitForNumberOfMembers(size)
   }

   def startAndWaitForLocalCacheManager() = {
      start()
      waitForLocalCacheManager()
   }

   def stop(): Unit = {
      shutDownServer()
      launcher.destroy()
   }

   def shutDownServer() = {
      runManagementOperation { client =>
         val op = new ModelNode
         op.get(OP).set(ShutDownOp)
         val res = client.execute(op)
         if (!(res.get(OUTCOME).asString() == SUCCESS)) {
            throw new Exception(s"Failure to stop server $name")
         }
      }
   }

   def getHotRodPort = if (portOffSet == 0) HotRodPort else ManagementPort + portOffSet

   def waitForNumberOfMembers(members: Int): Unit =
      waitForServerAttribute("/subsystem=infinispan/cache-container=clustered", "cluster-size", _.toInt == members)

   def waitForLocalCacheManager(): Unit =
      waitForServerAttribute("/subsystem=infinispan/cache-container=local", "cache-manager-status", _ == "RUNNING")

   def addCache(cacheContainer: String, cacheName: String, cacheType: CacheType.Value): Unit = {
      runManagementOperation { client =>
         val pathAddress = PathAddress.pathAddress(SUBSYSTEM, "infinispan")
                 .append("cache-container", cacheContainer)
                 .append(cacheType.toString, cacheName)
         val op: ModelNode = new ModelNode
         op.get(OP).set(ADD)
         op.get(OP_ADDR).set(pathAddress.toModelNode)
         op.get("start").set("EAGER")
         if (cacheType != CacheType.LOCAL) {
            op.get("mode").set("SYNC")
         }
         val resp: ModelNode = client.execute(op)
         if (!(SUCCESS == resp.get(OUTCOME).asString)) {
            throw new IllegalArgumentException(resp.asString)
         }
      }
   }

   private def runManagementOperation(op: ModelControllerClient => Unit) = {
      val client = ModelControllerClient.Factory.create(Protocol, InetAddress.getByName(Host), getManagementPort)
      op(client)
      client.close()
   }

   private def getManagementPort = if (portOffSet == 0) ManagementPort else ManagementPort + portOffSet

   @tailrec
   private def retry(command: => Boolean, numTimes: Int = 60, waitBetweenRetries: Int = 1000): Unit = {
      Try(command) match {
         case Success(true) =>
         case Success(false) if numTimes == 0 => throw new Exception("Timeout waiting for condition")
         case Success(false) =>
            Thread.sleep(waitBetweenRetries)
            retry(command, numTimes - 1, waitBetweenRetries)
         case Failure(e) if numTimes == 0 => throw e
         case Failure(e) if numTimes > 1 =>
            Thread.sleep(waitBetweenRetries)
            retry(command, numTimes - 1, waitBetweenRetries)
      }
   }

   private def waitForServerAttribute(path: String, name: String, condition: String => Boolean): Unit = {
      runManagementOperation { client =>
         val pathAddress = PathAddress.parseCLIStyleAddress(path)
         val op = new ModelNode
         op.get(OP).set(READ_ATTRIBUTE_OPERATION)
         op.get(OP_ADDR).set(pathAddress.toModelNode)
         op.get("name").set(name)
         retry {
            val res = client.execute(op)
            if (res.get(OUTCOME).asString() == SUCCESS) {
               val attributeValue = res.get(RESULT)
               condition(attributeValue.asString())
            } else {
               false
            }
         }
      }
   }
}

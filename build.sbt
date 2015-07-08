import sbt.Keys._

name := "infinispan-spark"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.10.4", "2.11.5")

libraryDependencies ++= {
   val infinispanVersion = "8.0.0.Beta1"
   val sparkVersion = "1.4.0"
   val scalaTestVersion = "2.2.5"
   val wildFlyControllerVersion = "1.0.0.CR6"
   Seq(
      // HotRod Client
      "org.infinispan" % "infinispan-client-hotrod" % infinispanVersion,
      "net.jcip" % "jcip-annotations" % "1.0",

      // Spark
      "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",

      // Test
      "org.infinispan" % "infinispan-client-hotrod" % infinispanVersion % "test" classifier "tests",
      "org.infinispan.server" % "infinispan-server-build" % infinispanVersion % "test" artifacts Artifact("infinispan-server-build", "zip", "zip"),
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
      "org.wildfly.core" % "wildfly-controller-client" % wildFlyControllerVersion % "test",
      "org.wildfly.core" % "wildfly-controller" % wildFlyControllerVersion % "test"
   )
}

parallelExecution in Test := false

publishArtifact in Test := true

val extractServer = taskKey[Seq[File]]("Extract infinispan server")

extractServer := {
   val report = update.value
   val deps = report.matching(artifactFilter(name = "infinispan-server-build", extension = "zip"))
   val zipPath = deps.head.getAbsoluteFile
   val destination = (resourceManaged in Test).value
   val destinationWithoutVersion = destination / "infinispan-server"
   IO.unzip(zipPath, destination).toSeq
   (destination ** "*infinispan-server*").get.head.renameTo(destinationWithoutVersion)
   (destinationWithoutVersion ** AllPassFilter).get
}

resourceGenerators in Test <+= extractServer

//javaOptions in Test := Seq("-XX:+UnlockCommercialFeatures","-XX:+FlightRecorder","-XX:StartFlightRecording=duration=60s,filename=myrecording.jfr,settings=profile")

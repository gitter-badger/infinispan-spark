name := "infinispan-spark"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.10.4", "2.11.5")

libraryDependencies ++= {
   val infinispanVersion = "8.0.0.Beta1"
   val sparkVersion = "1.4.0"
   val scalaTestVersion = "2.2.5"
   Seq(
      "org.infinispan" % "infinispan-client-hotrod" % infinispanVersion,
      "net.jcip" % "jcip-annotations" % "1.0",

      "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",

      "org.infinispan" % "infinispan-client-hotrod" % infinispanVersion % "test" classifier "tests",
      "org.infinispan" % "infinispan-server-hotrod" % infinispanVersion % "test" classifier "tests",
      "org.infinispan" % "infinispan-server-hotrod" % infinispanVersion % "test",
      "org.infinispan" % "infinispan-core" % infinispanVersion % "test" classifier "tests",
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
   )
}

resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"

parallelExecution in Test := false

fork := true

publishArtifact in Test := true

//javaOptions in Test := Seq("-XX:+UnlockCommercialFeatures","-XX:+FlightRecorder","-XX:StartFlightRecording=duration=60s,filename=myrecording.jfr,settings=profile")

name := "infinispan-spark"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.10.4", "2.11.5")

libraryDependencies ++= Seq(
   "org.apache.spark" %% "spark-core" % "1.4.0" % "provided",
   "org.apache.spark" %% "spark-sql" % "1.4.0" % "provided",
   "org.apache.spark" %% "spark-streaming" % "1.4.0" % "provided",
   "org.infinispan" % "infinispan-client-hotrod" % "8.0.0-SNAPSHOT",
   "org.infinispan" % "infinispan-client-hotrod" % "8.0.0-SNAPSHOT" % "test" classifier "tests",
   "org.infinispan" % "infinispan-server-hotrod" % "8.0.0-SNAPSHOT" % "test" classifier "tests",
   "org.infinispan" % "infinispan-server-hotrod" % "8.0.0-SNAPSHOT" % "test",
   "org.infinispan" % "infinispan-core" % "8.0.0-SNAPSHOT" % "test" classifier "tests",
   "org.scalatest" %% "scalatest" % "2.2.5" % "test"
)

resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"

parallelExecution in Test := false

fork := true

publishArtifact in Test := true

//javaOptions in Test := Seq("-XX:+UnlockCommercialFeatures","-XX:+FlightRecorder","-XX:StartFlightRecording=duration=60s,filename=myrecording.jfr,settings=profile")


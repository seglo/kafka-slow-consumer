import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.seglo",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "kafka-slow-consumer",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream-kafka" % "0.17",
      scalaTest % Test
    )
  )

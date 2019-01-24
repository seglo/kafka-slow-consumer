import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}
import Dependencies._

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(
    inThisBuild(List(
      organization := "com.seglo",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "kafka-slow-consumer",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream-kafka" % "0.22",
      scalaTest % Test
    ),
    dockerUsername := Some("seglo"),
    // Based on best practices found in OpenShift Creating images guidelines
    // https://docs.openshift.com/container-platform/3.10/creating_images/guidelines.html
    dockerCommands := Seq(
      Cmd("FROM",           "centos:7"),
      Cmd("RUN",            "yum -y install java-1.8.0-openjdk-headless && yum clean all -y"),
      Cmd("RUN",            "useradd -r -m -u 1001 -g 0 kafkaslowconsumer"),
      Cmd("ADD",            "opt /opt"),
      Cmd("RUN",            "chgrp -R 0 /opt && chmod -R g=u /opt"),
      Cmd("WORKDIR",        "/opt/docker"),
      Cmd("USER",           "1001"),
      ExecCmd("CMD",        "/opt/docker/bin/kafka-slow-consumer", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap")
    )
  )
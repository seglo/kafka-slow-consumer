import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}
import Dependencies._

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(JavaAgent)
  .enablePlugins(DockerPlugin)
  .settings(
    inThisBuild(List(
      organization := "com.seglo",
      scalaVersion := "2.12.8",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "kafka-slow-consumer",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-slf4j" % "2.5.23",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.slf4j" % "log4j-over-slf4j" % "1.7.26",
      "com.typesafe.akka" %% "akka-stream-kafka" % "1.0.4+5-fcff1ccc+20190626-1606",
      scalaTest % Test
    ),
    javaAgents += JavaAgent("io.prometheus.jmx" % "jmx_prometheus_javaagent" % "0.11.0", arguments = "8080:/opt/docker/conf/jmx-prometheus-exporter-config.yaml"),
    mappings in Universal += file("conf/jmx-prometheus-exporter-config.yaml") -> "conf/jmx-prometheus-exporter-config.yaml",
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
      ExecCmd("CMD",        "/opt/docker/bin/kafka-slow-consumer")
    )
  )

resolvers in ThisBuild += Resolver.mavenLocal

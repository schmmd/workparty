name := "workparty"

scalaVersion := "2.10.1"

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "2.1.0",
  "com.jsuereth" %% "scala-arm" % "1.3",
  "com.typesafe.akka" %% "akka-actor" % "2.1.4",
  "org.slf4j" % "slf4j-api" % "1.7.2",
  "ch.qos.logback" % "logback-classic" % "1.0.9" % "test",
  "ch.qos.logback" % "logback-core" % "1.0.9" % "test")

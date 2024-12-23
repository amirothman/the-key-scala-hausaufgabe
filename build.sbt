name := """the-key-scala-hausaufgabe"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.15"

libraryDependencies ++= Seq(
  guice,
  "com.softwaremill.sttp.client3" %% "core" % "3.8.15",
  "com.softwaremill.sttp.client3" %% "play-json" % "3.8.15",
  "com.typesafe.play" %% "play-json" % "2.10.0",
  "org.apache.pekko" %% "pekko-stream" % "1.0.3",
  "org.apache.pekko" %% "pekko-stream-testkit" % "1.0.3" % Test,
  "org.apache.pekko" %% "pekko-testkit" % "1.0.3" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"

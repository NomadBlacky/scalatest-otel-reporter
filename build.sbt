lazy val scala2_13              = "2.13.12"
lazy val scala3                 = "3.3.1"
lazy val supportedScalaVersions = List(scala2_13, scala3)

ThisBuild / scalaVersion       := scala3
ThisBuild / crossScalaVersions := supportedScalaVersions

lazy val root = (project in file("."))
  .aggregate(`scalatest-otel-reporter`)
  .settings(
    publish / skip := true,
  )

lazy val `scalatest-otel-reporter` = (project in file("scalatest-otel-reporter"))
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest"   %% "scalatest"         % "3.2.16" % Provided,
      "io.opentelemetry" % "opentelemetry-sdk" % "1.30.0" % Provided,
    ),
  )

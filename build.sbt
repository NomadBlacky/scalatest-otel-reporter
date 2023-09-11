ThisBuild / scalaVersion := "3.3.1"

lazy val `scalatest-otel-reporter` = (project in file("scalatest-otel-reporter"))
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest"   %% "scalatest"         % "3.2.16" % Provided,
      "io.opentelemetry" % "opentelemetry-sdk" % "1.30.0" % Provided,
    ),
  )

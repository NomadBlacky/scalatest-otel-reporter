import Dependencies._

lazy val scala2_13              = "2.13.12"
lazy val scala3                 = "3.3.3"
lazy val supportedScalaVersions = List(scala2_13, scala3)

ThisBuild / scalaVersion       := scala3
ThisBuild / crossScalaVersions := supportedScalaVersions

ThisBuild / organization := "dev.nomadblacky"
ThisBuild / homepage     := Some(url("https://github.com/NomadBlacky/scalatest-otel-reporter"))
ThisBuild / licenses     := List(License.MIT)
ThisBuild / developers := List(
  Developer("nomadblacky", "Takumi Kadowaki", "nomadblacky@gmail.com", url("https://github.com/NomadBlacky")),
)

// sbt-github-actions
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"), JavaSpec.temurin("11"))
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.StartsWith(Ref.Tag("v")),
  RefPredicate.Equals(Ref.Branch("main")),
)
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    commands = List("ci-release"),
    name = Some("Publish project"),
    env = Map(
      "PGP_PASSPHRASE"    -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"        -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}",
    ),
  ),
)
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(
    List("scalafmtSbtCheck", "scalafmtCheckAll", "test"),
    name = Some("Build project"),
  ),
)

lazy val root = (project in file("."))
  .aggregate(`scalatest-otel-reporter`, `example-manual-configuration`)
  .settings(
    publish / skip := true,
  )

lazy val `scalatest-otel-reporter` = (project in file("scalatest-otel-reporter"))
  .settings(
    libraryDependencies ++= Seq(
      scalatest % Provided,
      otelSdk   % Provided,
    ),
  )

lazy val `example-manual-configuration` = (project in file("examples/manual-configuration"))
  .dependsOn(`scalatest-otel-reporter`)
  .settings(
    publish / skip := true,
    libraryDependencies ++= Seq(
      scalatest        % Test,
      otelSdk          % Test,
      otelExporterOTLP % Test,
      otelSemConv      % Test,
    ),
    Test / testOptions += Tests.Argument(
      TestFrameworks.ScalaTest,
      "-C",
      "dev.nomadblacky.scalatest_otel_reporter.examples.JaegerTestReporter",
    ),
  )

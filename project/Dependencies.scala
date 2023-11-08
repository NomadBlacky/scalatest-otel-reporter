import sbt._

object Dependencies {
  private val v = new {
    val otel = "1.30.1"
  }

  // scalatest
  val scalatest = "org.scalatest" %% "scalatest" % "3.2.17"

  // OpenTelemetry
  val otelSdk           = "io.opentelemetry"         % "opentelemetry-sdk"                         % v.otel
  val otelExporterOTLP  = "io.opentelemetry"         % "opentelemetry-exporter-otlp"               % v.otel
  val otelAutoConfigure = "io.opentelemetry"         % "opentelemetry-sdk-extension-autoconfigure" % v.otel
  val otelSemConv       = "io.opentelemetry.semconv" % "opentelemetry-semconv"                     % "1.21.0-alpha"

  // Testing utilities
  val wiremock       = "org.wiremock"       % "wiremock"       % "3.1.0"
  val testcontainers = "org.testcontainers" % "testcontainers" % "1.19.0"
}

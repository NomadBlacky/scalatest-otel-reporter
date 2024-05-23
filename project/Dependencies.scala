import sbt.*

object Dependencies {
  private val v = new {
    val otel = "1.38.0"
  }

  // scalatest
  val scalatest = "org.scalatest" %% "scalatest" % "3.2.18"

  // OpenTelemetry
  val otelSdk           = "io.opentelemetry"         % "opentelemetry-sdk"                         % v.otel
  val otelExporterOTLP  = "io.opentelemetry"         % "opentelemetry-exporter-otlp"               % v.otel
  val otelAutoConfigure = "io.opentelemetry"         % "opentelemetry-sdk-extension-autoconfigure" % v.otel
  val otelSemConv       = "io.opentelemetry.semconv" % "opentelemetry-semconv"                     % "1.21.0-alpha"
  val otelProto         = "io.opentelemetry.proto"   % "opentelemetry-proto"                       % "1.1.0-alpha"

  // Testing utilities
  val wiremock       = "org.wiremock"       % "wiremock"       % "3.4.2"
  val testcontainers = "org.testcontainers" % "testcontainers" % "1.19.0"

  // Others
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.5.6"
  val julToSlf4j     = "org.slf4j"      % "jul-to-slf4j"    % "2.0.12"
}

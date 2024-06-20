package dev.nomadblacky.scalatest_otel_reporter

import io.opentelemetry.api.OpenTelemetry

import java.util.logging.Logger

trait OpenTelemetryApiTestReporter extends OpenTelemetryTestReporter[OpenTelemetry] {
  private lazy val logger: Logger = Logger.getLogger(classOf[OpenTelemetryApiTestReporter].getName)

  protected def shutdownOtel(): Unit = {
    // OpenTelemetryApiTestReporter does not shutdown OpenTelemetry because OpenTelemetry API does not have a shutdown method.
    // If you are using something like the OpenTelemetry Java Agent, it will automatically shutdown when the process terminates.
    // However, in other environments, it will not run shutdown, so if the process terminates immediately after the test ends, telemetry data may be lost.
  }
}

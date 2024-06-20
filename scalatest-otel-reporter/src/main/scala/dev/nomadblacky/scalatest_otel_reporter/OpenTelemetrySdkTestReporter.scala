package dev.nomadblacky.scalatest_otel_reporter
import io.opentelemetry.sdk.OpenTelemetrySdk

import java.util.logging.Logger

trait OpenTelemetrySdkTestReporter extends OpenTelemetryTestReporter[OpenTelemetrySdk] {
  private lazy val logger = Logger.getLogger(classOf[OpenTelemetrySdkTestReporter].getName)

  override protected def shutdownOtel(): Unit = {
    logger.info("Shutting down OpenTelemetry")
    otel.close()
  }
}

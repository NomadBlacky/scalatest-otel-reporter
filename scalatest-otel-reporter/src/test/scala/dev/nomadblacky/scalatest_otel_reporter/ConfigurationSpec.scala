package dev.nomadblacky.scalatest_otel_reporter

import org.scalatest.{ConfigMap, ParallelTestExecution}
import org.scalatest.events.{Ordinal, RunCompleted, RunStarting}

class ConfigurationSpec extends UnitTestSuite with OTelMockServer with ParallelTestExecution {
  import OpenTelemetryTestReporter._

  test("Configure the root span name") {
    val spans = instrumentWithMockServer { mockServer =>
      val reporter = new WireMockOTelTestReporter(host, mockServer.port())
      reporter.apply(RunStarting(new Ordinal(0), 0, new ConfigMap(Map(ConfigKeyRootSpanName -> "my-awesome-tests"))))
      reporter.apply(RunCompleted(ordinal = new Ordinal(1)))
    }

    logger.info(s"Received spans: $spans")

    assert(spans.size == 1)
    val span = spans.head
    span.getName shouldBe "my-awesome-tests"
  }

  test("Fallback to the default root span name") {
    val spans = instrumentWithMockServer { mockServer =>
      val reporter = new WireMockOTelTestReporter(host, mockServer.port())
      reporter.apply(RunStarting(new Ordinal(0), 0, new ConfigMap(Map.empty)))
      reporter.apply(RunCompleted(ordinal = new Ordinal(1)))
    }

    logger.info(s"Received spans: $spans")

    assert(spans.size == 1)
    val span = spans.head
    span.getName shouldBe DefaultRootSpanName
  }
}

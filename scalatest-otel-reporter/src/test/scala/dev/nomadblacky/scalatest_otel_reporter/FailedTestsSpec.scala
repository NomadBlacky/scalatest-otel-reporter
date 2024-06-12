package dev.nomadblacky.scalatest_otel_reporter

import io.opentelemetry.proto.trace.v1.Status
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.{Args, ParallelTestExecution}

import scala.jdk.CollectionConverters.CollectionHasAsScala

class FailedTestsSpec extends UnitTestSuite with OTelMockServer with ParallelTestExecution {

  test("failed assertion") {
    class FailedAssertion extends AnyFunSuiteLike {
      test("failed assertion") {
        assert(1 + 1 == 3)
      }
    }

    val spans = instrumentWithMockServer { mockServer =>
      (new FailedAssertion).run(None, Args(reporter = new WireMockOTelTestReporter(host, mockServer.port())))
    }

    logger.info(s"Received spans: $spans")

    assert(spans.size == 1)
    val span = spans.head
    span.getName shouldBe "failed assertion"
    span.getStatus.getCode shouldBe Status.StatusCode.STATUS_CODE_ERROR
    span.getStatus.getMessage shouldBe "org.scalatest.exceptions.TestFailedException was thrown."
    val exceptionAttr = span.getEvents(0).getAttributes(0)
    exceptionAttr.getKey shouldBe "exception.stacktrace"
    exceptionAttr.getValue.getStringValue should startWith("org.scalatest.exceptions.TestFailedException")
  }

  test("throw an exception") {
    class ThrowAnException extends AnyFunSuiteLike {
      test("throw an exception") {
        throw new RuntimeException("An exception occurred")
      }
    }

    val spans = instrumentWithMockServer { mockServer =>
      (new ThrowAnException).run(None, Args(reporter = new WireMockOTelTestReporter(host, mockServer.port())))
    }

    logger.info(s"Received spans: $spans")

    assert(spans.size == 1)
    val span = spans.head
    span.getName shouldBe "throw an exception"
    span.getStatus.getCode shouldBe Status.StatusCode.STATUS_CODE_ERROR
    span.getStatus.getMessage shouldBe "An exception occurred"
    val attributes = span.getEvents(0).getAttributesList.asScala
    assert(attributes.exists { kv =>
      kv.getKey == "exception.type" && kv.getValue.getStringValue == "java.lang.RuntimeException"
    })
    assert(attributes.exists { kv =>
      kv.getKey == "exception.message" && kv.getValue.getStringValue == "An exception occurred"
    })
    assert(attributes.exists { kv =>
      kv.getKey == "exception.stacktrace" && kv.getValue.getStringValue.startsWith("java.lang.RuntimeException")
    })
  }
}

package dev.nomadblacky.scalatest_otel_reporter

import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.{Args, ParallelTestExecution}

class IncompleteTestsSpec extends UnitTestSuite with OTelMockServer with ParallelTestExecution {

  test("ignore test") {
    class IgnoreTest extends AnyFunSuiteLike {
      ignore("ignore test") {
        assert(1 + 1 == 2)
      }
    }

    val spans = instrumentWithMockServer { mockServer =>
      (new IgnoreTest).run(None, Args(reporter = new WireMockOTelTestReporter(host, mockServer.port())))
    }

    logger.info(s"Received spans: $spans")

    assert(spans.size == 1)
    val span = spans.head
    span.getName shouldBe "ignore test"
  }

  test("pending test") {
    class PendingTest extends AnyFunSuiteLike {
      test("pending test") {
        pending
      }
    }

    val spans = instrumentWithMockServer { mockServer =>
      (new PendingTest).run(None, Args(reporter = new WireMockOTelTestReporter(host, mockServer.port())))
    }

    logger.info(s"Received spans: $spans")

    assert(spans.size == 1)
    val span = spans.head
    span.getName shouldBe "pending test"
  }

  test("canceled test") {
    class CanceledTest extends AnyFunSuiteLike {
      test("canceled test") {
        cancel("canceled test")
      }
    }

    val spans = instrumentWithMockServer { mockServer =>
      (new CanceledTest).run(None, Args(reporter = new WireMockOTelTestReporter(host, mockServer.port())))
    }

    logger.info(s"Received spans: $spans")

    assert(spans.size == 1)
    val span = spans.head
    span.getName shouldBe "canceled test"
  }

  test("canceled test with exception") {
    class CanceledTestWithException extends AnyFunSuiteLike {
      test("canceled test with exception") {
        cancel("canceled test with exception", new RuntimeException("An exception occurred"))
      }
    }

    val spans = instrumentWithMockServer { mockServer =>
      (new CanceledTestWithException).run(None, Args(reporter = new WireMockOTelTestReporter(host, mockServer.port())))
    }

    logger.info(s"Received spans: $spans")

    assert(spans.size == 1)
    val span = spans.head
    span.getName shouldBe "canceled test with exception"
  }
}

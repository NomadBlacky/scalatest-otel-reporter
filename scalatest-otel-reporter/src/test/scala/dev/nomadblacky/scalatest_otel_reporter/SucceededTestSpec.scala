package dev.nomadblacky.scalatest_otel_reporter

import io.opentelemetry.proto.trace.v1.Status
import org.scalatest.Args
import org.scalatest.funspec.AnyFunSpecLike

class SucceededTestSpec extends UnitTestSuite with OTelMockServer {

  test("SimpleTest") {
    class SimpleTests extends AnyFunSpecLike {
      describe("Describe") {
        it("It") {
          assert(1 == 1)
        }
      }
    }

    val spans = instrumentWithMockServer { mockServer =>
      (new SimpleTests).run(None, Args(reporter = new WireMockOTelTestReporter(host, mockServer.port())))
    }

    logger.info(s"Received spans: $spans")

    assert(spans.size == 1)
    val span = spans.head
    span.getName shouldBe "Describe It"
    span.getStatus.getCode shouldBe Status.StatusCode.STATUS_CODE_OK
  }
}

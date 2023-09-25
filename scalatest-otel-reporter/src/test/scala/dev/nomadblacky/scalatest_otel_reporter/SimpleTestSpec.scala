package dev.nomadblacky.scalatest_otel_reporter

import org.scalatest.Args
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.funsuite.AnyFunSuiteLike

class SimpleTestSpec extends AnyFunSuiteLike {

  class SimpleTests extends AnyFunSpecLike {
    describe("Describe") {
      it("It") {
        Thread.sleep(1000)
        assert(1 == 1)
      }
    }
  }

  test("SimpleTest") {
    val simpleTests = new SimpleTests
    simpleTests.run(None, Args(new OpenTelemetryTestReporter))
  }
}

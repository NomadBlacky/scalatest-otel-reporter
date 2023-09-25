package dev.nomadblacky.scalatest_otel_reporter.examples

import org.scalatest.funspec.AnyFunSpecLike

class ManualConfigurationExample extends AnyFunSpecLike {

  describe("Describe") {
    it("It") {
      Thread.sleep(1000)
      assert(1 == 1)
    }
  }
}

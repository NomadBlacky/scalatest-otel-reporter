package dev.nomadblacky.scalatest_otel_reporter.examples

import org.scalatest.funsuite.AnyFunSuiteLike

class SucceededTests extends AnyFunSuiteLike {

  test("sum") {
    assert(1 + 1 == 2)
  }

  test("sub") {
    assert(1 - 1 == 0)
  }

}

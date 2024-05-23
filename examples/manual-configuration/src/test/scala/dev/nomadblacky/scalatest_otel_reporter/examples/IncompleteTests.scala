package dev.nomadblacky.scalatest_otel_reporter.examples

import org.scalatest.funsuite.AnyFunSuiteLike

class IncompleteTests extends AnyFunSuiteLike {

  ignore("ignore test") {
    assert(1 + 1 == 2)
  }

  test("pending test") {
    pending
  }

  test("canceled test") {
    cancel("canceled test")
  }

  test("canceled test with exception") {
    cancel("canceled test with exception", new RuntimeException("An exception occurred"))
  }
}

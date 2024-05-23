package dev.nomadblacky.scalatest_otel_reporter.examples

import org.scalatest.funsuite.AnyFunSuiteLike

class FailedTests extends AnyFunSuiteLike {

  test("failed assertion") {
    assert(1 + 1 == 3)
  }

  test("throw an exception") {
    throw new RuntimeException("An exception occurred")
  }

}

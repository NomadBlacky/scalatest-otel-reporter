package dev.nomadblacky.scalatest_otel_reporter

import org.scalatest.Inside
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers
import org.slf4j.bridge.SLF4JBridgeHandler

import java.util.logging.Logger

trait UnitTestSuite extends AnyFunSuiteLike with Matchers with Inside {
  protected val logger: Logger = Logger.getLogger(getClass.getName)
}

object UnitTestSuite {
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()
}

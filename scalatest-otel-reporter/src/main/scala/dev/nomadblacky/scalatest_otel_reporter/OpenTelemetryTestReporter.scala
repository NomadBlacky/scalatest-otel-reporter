package dev.nomadblacky.scalatest_otel_reporter

import io.opentelemetry.api.trace.{Span, StatusCode}
import io.opentelemetry.api.{GlobalOpenTelemetry, OpenTelemetry}
import io.opentelemetry.context.Context
import org.scalatest.Reporter
import org.scalatest.events._

import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

trait BaseOpenTelemetryTestReporter extends Reporter {
  def otel: OpenTelemetry

  private val tracer = otel.getTracerProvider.get("scalatest")
  private val logger = Logger.getLogger(classOf[BaseOpenTelemetryTestReporter].getName)

  // TODO: Extract spans to a container class
  private var testRootSpan: Span = _
  private val suitesMap          = new ConcurrentHashMap[String, Span]()
  private val testsMap           = new ConcurrentHashMap[String, Span]()

  def apply(event: Event): Unit =
    event match {
      /*
       * Run events
       */
      case starting: RunStarting =>
        logger.fine(s"RunStarting")
        // TODO: Make possible to configure the root span name
        testRootSpan = tracer.spanBuilder("UNIT_TEST").startSpan()

      case completed: RunCompleted =>
        logger.fine(s"RunCompleted")
        Option(testRootSpan).fold(throw new IllegalStateException("TestRootSpan not found")) { span =>
          span.end()
        }

      case stopped: RunStopped =>
        logger.fine(s"RunStopped")
        Option(testRootSpan).fold(throw new IllegalStateException("TestRootSpan not found")) { span =>
          span.end()
        }

      case aborted: RunAborted =>
        logger.fine(s"RunAborted")
        Option(testRootSpan).fold(throw new IllegalStateException("TestRootSpan not found")) { span =>
          span
            .setStatus(StatusCode.ERROR, aborted.message)
            .recordException(aborted.throwable.orNull)
            .end()
        }

      /*
       * Suite events
       */
      case starting: SuiteStarting =>
        logger.fine(s"SuiteStarting: ${starting.suiteName}")
        val suiteSpan =
          tracer.spanBuilder(starting.suiteName).setParent(Context.current().`with`(testRootSpan)).startSpan()
        suitesMap.put(starting.suiteId, suiteSpan)

      case completed: SuiteCompleted =>
        logger.fine(s"SuiteCompleted: ${completed.suiteName}")
        Option(suitesMap.remove(completed.suiteId))
          .fold(throw new IllegalStateException(s"Suite not found: $completed")) { span =>
            span
              .setStatus(StatusCode.OK)
              .end()
          }

      case aborted: SuiteAborted =>
        logger.fine(s"SuiteAborted: ${aborted.suiteName}")
        Option(suitesMap.remove(aborted.suiteId))
          .fold(throw new IllegalStateException(s"Suite not found: $aborted")) { span =>
            span
              .setStatus(StatusCode.ERROR, aborted.message)
              .recordException(aborted.throwable.orNull)
              .end()
          }

      /*
       * Test events
       */
      case starting: TestStarting =>
        logger.fine(s"TestStarting: ${starting.testName}")
        val suiteSpan     = Option(suitesMap.get(starting.suiteId))
        val parentContext = suiteSpan.fold(Context.current())(Context.current().`with`)
        val testSpan      = tracer.spanBuilder(starting.testName).setParent(parentContext).startSpan()
        testsMap.put(starting.testName, testSpan)

      case succeeded: TestSucceeded =>
        logger.fine(s"TestSucceeded: ${succeeded.testName}")
        Option(testsMap.remove(succeeded.testName))
          .fold(throw new IllegalStateException(s"Test not found: $succeeded")) { span =>
            span
              .setStatus(StatusCode.OK)
              .end()
          }

      case failed: TestFailed =>
        logger.fine(s"TestFailed: ${failed.testName}")
        Option(testsMap.remove(failed.testName))
          .fold(throw new IllegalStateException(s"Test not found: ${failed.testName}")) { span =>
            span
              .setStatus(StatusCode.ERROR, failed.message)
              .recordException(failed.throwable.orNull)
              .end()
          }

      case ignored: TestIgnored =>
        logger.fine(s"TestIgnored: ${ignored.testName}")
        val suiteSpan     = Option(suitesMap.get(ignored.suiteId))
        val parentContext = suiteSpan.fold(Context.current())(Context.current().`with`)
        val testSpan      = tracer.spanBuilder(ignored.testName).setParent(parentContext).startSpan()
        testSpan.end()

      case pending: TestPending =>
        logger.fine(s"TestPending: ${pending.testName}")
        val suiteSpan     = Option(suitesMap.get(pending.suiteId))
        val parentContext = suiteSpan.fold(Context.current())(Context.current().`with`)
        val testSpan      = tracer.spanBuilder(pending.testName).setParent(parentContext).startSpan()
        testSpan.end()

      case canceled: TestCanceled =>
        logger.fine(s"TestCanceled: ${canceled.testName}")
        Option(testsMap.remove(canceled.testName))
          .fold(throw new IllegalStateException(s"Test not found: ${canceled.testName}")) { span =>
            span.recordException(canceled.throwable.orNull).end()
          }

      /*
       * Other events
       */
      case _: ScopeOpened        => ()
      case _: ScopeClosed        => ()
      case _: ScopePending       => ()
      case _: DiscoveryStarting  => ()
      case _: DiscoveryCompleted => ()
      case _: InfoProvided       => ()
      case _: AlertProvided      => ()
      case _: NoteProvided       => ()
      case _: MarkupProvided     => ()
    }
}

class OpenTelemetryTestReporter extends BaseOpenTelemetryTestReporter {
  def otel: OpenTelemetry = GlobalOpenTelemetry.get()
}

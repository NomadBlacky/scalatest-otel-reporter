package dev.nomadblacky.scalatest_otel_reporter

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.{Span, StatusCode}
import io.opentelemetry.context.Context
import org.scalatest.Reporter
import org.scalatest.events._

import java.util.logging.Logger
import scala.collection.concurrent.TrieMap

trait OpenTelemetryTestReporter[A <: OpenTelemetry] extends Reporter {
  import OpenTelemetryTestReporter._

  def initOpenTelemetry: A
  protected def shutdownOtel(): Unit

  protected lazy val otel: A = initOpenTelemetry

  private lazy val tracer = otel.getTracerProvider.get("scalatest")
  private lazy val logger = Logger.getLogger(classOf[OpenTelemetryTestReporter.type].getName)

  private var testRootSpan: Option[Span] = None
  private val suitesMap                  = TrieMap.empty[String, Span]
  private val testsMap                   = TrieMap.empty[String, Span]

  def apply(event: Event): Unit =
    event match {
      /*
       * Run events
       */
      case starting: RunStarting =>
        logger.fine(s"RunStarting")
        val rootSpanName = starting.configMap.getWithDefault(ConfigKeyRootSpanName, DefaultRootSpanName)
        testRootSpan = Some(tracer.spanBuilder(rootSpanName).startSpan())

      case completed: RunCompleted =>
        logger.fine(s"RunCompleted")
        testRootSpan.fold(throw new IllegalStateException("TestRootSpan not found")) { span =>
          span.end()
        }
        shutdownOtel()

      case stopped: RunStopped =>
        logger.fine(s"RunStopped")
        testRootSpan.fold(throw new IllegalStateException("TestRootSpan not found")) { span =>
          span.end()
        }
        shutdownOtel()

      case aborted: RunAborted =>
        logger.fine(s"RunAborted")
        testRootSpan.fold(throw new IllegalStateException("TestRootSpan not found")) { span =>
          span
            .setStatus(StatusCode.ERROR, aborted.message)
            .recordException(aborted.throwable.orNull)
            .end()
        }
        shutdownOtel()

      /*
       * Suite events
       */
      case starting: SuiteStarting =>
        logger.fine(s"SuiteStarting: ${starting.suiteName}")
        testRootSpan.fold(throw new IllegalStateException("TestRootSpan not found")) { rootSpan =>
          val suiteSpan =
            tracer.spanBuilder(starting.suiteName).setParent(Context.current().`with`(rootSpan)).startSpan()
          suitesMap.put(starting.suiteId, suiteSpan)
        }

      case completed: SuiteCompleted =>
        logger.fine(s"SuiteCompleted: ${completed.suiteName}")
        suitesMap
          .remove(completed.suiteId)
          .fold(throw new IllegalStateException(s"Suite not found: $completed")) { span =>
            span
              .setStatus(StatusCode.OK)
              .end()
          }

      case aborted: SuiteAborted =>
        logger.fine(s"SuiteAborted: ${aborted.suiteName}")
        suitesMap
          .remove(aborted.suiteId)
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
        val suiteSpan     = suitesMap.get(starting.suiteId)
        val parentContext = suiteSpan.fold(Context.current())(Context.current().`with`)
        val testSpan      = tracer.spanBuilder(starting.testName).setParent(parentContext).startSpan()
        testsMap.put(starting.testName, testSpan)

      case succeeded: TestSucceeded =>
        logger.fine(s"TestSucceeded: ${succeeded.testName}")
        testsMap
          .remove(succeeded.testName)
          .fold(throw new IllegalStateException(s"Test not found: $succeeded")) { span =>
            span
              .setStatus(StatusCode.OK)
              .end()
          }

      case failed: TestFailed =>
        logger.fine(s"TestFailed: ${failed.testName}")
        testsMap
          .remove(failed.testName)
          .fold(throw new IllegalStateException(s"Test not found: ${failed.testName}")) { span =>
            span
              .setStatus(StatusCode.ERROR, failed.message)
              .recordException(failed.throwable.orNull)
              .end()
          }

      case ignored: TestIgnored =>
        logger.fine(s"TestIgnored: ${ignored.testName}")
        val suiteSpan     = suitesMap.get(ignored.suiteId)
        val parentContext = suiteSpan.fold(Context.current())(Context.current().`with`)
        val testSpan      = tracer.spanBuilder(ignored.testName).setParent(parentContext).startSpan()
        testSpan.end()

      case pending: TestPending =>
        logger.fine(s"TestPending: ${pending.testName}")
        val suiteSpan     = suitesMap.get(pending.suiteId)
        val parentContext = suiteSpan.fold(Context.current())(Context.current().`with`)
        val testSpan      = tracer.spanBuilder(pending.testName).setParent(parentContext).startSpan()
        testSpan.end()

      case canceled: TestCanceled =>
        logger.fine(s"TestCanceled: ${canceled.testName}")
        testsMap
          .remove(canceled.testName)
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

object OpenTelemetryTestReporter {
  val ConfigKeyRootSpanName = "scalatest-otel-reporter.root-span-name"
  val DefaultRootSpanName   = "scalatest"
}

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

  private var testRunSpan: Span = _
  private val suitesMap         = new ConcurrentHashMap[String, Span]()
  private val testsMap          = new ConcurrentHashMap[String, Span]()

  def apply(event: Event): Unit =
    event match {
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
            span.setStatus(StatusCode.OK)
            span.end()
          }
      case _: TestFailed =>
      // TODO
      case ignored: TestIgnored =>
      // TODO
      case pending: TestPending =>
      // TODO
      case canceled: TestCanceled =>
      // TODO
      case starting: SuiteStarting =>
        logger.fine(s"SuiteStarting: ${starting.suiteName}")
        val suiteSpan =
          tracer.spanBuilder(starting.suiteName).setParent(Context.current().`with`(testRunSpan)).startSpan()
        suitesMap.put(starting.suiteId, suiteSpan)
      case completed: SuiteCompleted =>
        logger.fine(s"SuiteCompleted: ${completed.suiteName}")
        val span = suitesMap.remove(completed.suiteId)
        span.end()
      case aborted: SuiteAborted =>
      // TODO
      case starting: RunStarting =>
        logger.fine(s"RunStarting")
        testRunSpan = tracer.spanBuilder("UNIT_TEST").startSpan()
      case completed: RunCompleted =>
        logger.fine(s"RunCompleted")
        testRunSpan.end()
      case stopped: RunStopped =>
      // TODO
      case aborted: RunAborted =>
      // TODO
      case opened: ScopeOpened =>
      // TODO
      case closed: ScopeClosed =>
      // TODO
      case pending: ScopePending =>
      // TODO
      case _: DiscoveryStarting  => ()
      case _: DiscoveryCompleted => ()
      case _: RecordableEvent    => ()
      case _: ExceptionalEvent   => ()
      case _: NotificationEvent  => ()
      case _: InfoProvided       => ()
      case _: AlertProvided      => ()
      case _: NoteProvided       => ()
      case _: MarkupProvided     => ()
    }
}

class OpenTelemetryTestReporter extends BaseOpenTelemetryTestReporter {
  def otel: OpenTelemetry = GlobalOpenTelemetry.get()
}

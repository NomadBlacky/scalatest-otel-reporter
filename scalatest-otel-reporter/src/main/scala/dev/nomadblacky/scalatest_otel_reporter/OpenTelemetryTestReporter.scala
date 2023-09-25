package dev.nomadblacky.scalatest_otel_reporter

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.{GlobalOpenTelemetry, OpenTelemetry}
import io.opentelemetry.context.Context
import org.scalatest.Reporter
import org.scalatest.events._

import java.util.concurrent.ConcurrentHashMap

trait BaseOpenTelemetryTestReporter extends Reporter {
  def otel: OpenTelemetry

  private val tracer = otel.getTracerProvider.get("scalatest")

  private var testRunSpan: Span = _
  private val suitesMap         = new ConcurrentHashMap[String, Span]()
  private val testsMap          = new ConcurrentHashMap[String, Span]()

  def apply(event: Event): Unit =
    event match {
      case starting: TestStarting =>
        val suiteSpan     = Option(suitesMap.get(starting.suiteId))
        val parentContext = suiteSpan.fold(Context.current())(Context.current().`with`)
        val testSpan      = tracer.spanBuilder(starting.testName).setParent(parentContext).startSpan()
        testsMap.put(starting.testName, testSpan)
      case succeeded: TestSucceeded =>
        Option(testsMap.remove(succeeded.testName))
          .fold(throw new IllegalStateException(s"Test not found: $succeeded"))(_.end())
      case _: TestFailed =>
      // TODO
      case ignored: TestIgnored =>
      // TODO
      case pending: TestPending =>
      // TODO
      case canceled: TestCanceled =>
      // TODO
      case starting: SuiteStarting =>
        val suiteSpan =
          tracer.spanBuilder(starting.suiteName).setParent(Context.current().`with`(testRunSpan)).startSpan()
        suitesMap.put(starting.suiteId, suiteSpan)
      case completed: SuiteCompleted =>
        val span = suitesMap.remove(completed.suiteId)
        span.end()
      case aborted: SuiteAborted =>
      // TODO
      case starting: RunStarting =>
        testRunSpan = tracer.spanBuilder("UNIT_TEST").startSpan()
      case completed: RunCompleted =>
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

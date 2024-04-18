package dev.nomadblacky.scalatest_otel_reporter

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest
import io.opentelemetry.proto.trace.v1.{Span, Status}
import org.scalactic.Equality
import org.scalatest.{Args, Inside}
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers
import org.slf4j.bridge.SLF4JBridgeHandler

import scala.jdk.CollectionConverters.CollectionHasAsScala

class SimpleTestSpec extends AnyFunSuiteLike with MockServer with Matchers with Inside {

  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  implicit val spanEquality: Equality[Span] = (a: Span, b: Any) =>
    b match {
      case bb: Span =>
        a.getName == bb.getName &&
        a.getStatus == bb.getStatus &&
        a.getAttributesList == bb.getAttributesList
      case _ =>
        false
    }

  class SimpleTests extends AnyFunSpecLike {
    describe("Describe") {
      it("It") {
        assert(1 == 1)
      }
    }
  }

  test("SimpleTest") {
    WireMock.stubFor(
      WireMock
        .post(WireMock.urlEqualTo("/"))
        .willReturn(WireMock.aResponse().withStatus(200)),
    )
    val simpleTests = new SimpleTests
    simpleTests.run(None, Args(reporter = new WireMockOTelTestReporter(this)))

    // Wait for sending spans
    Thread.sleep(10000L)

    val spans =
      WireMock
        .findAll(RequestPatternBuilder.allRequests())
        .asScala
        .flatMap(req => ExportTraceServiceRequest.parseFrom(req.getBody).getResourceSpansList.asScala)
        .flatMap(_.getScopeSpansList.asScala)
        .flatMap(_.getSpansList.asScala)

    spans should contain(
      Span
        .newBuilder()
        .setName("Describe It")
        .setStatus(Status.newBuilder.setCode(Status.StatusCode.STATUS_CODE_OK).build())
        .build(),
    )
  }
}

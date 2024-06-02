package dev.nomadblacky.scalatest_otel_reporter

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest
import io.opentelemetry.proto.trace.v1.Span
import org.scalatest.Suite

import scala.jdk.CollectionConverters.CollectionHasAsScala

trait OTelMockServer { self: Suite =>

  def host: String = "localhost"

  def config: WireMockConfiguration = wireMockConfig().bindAddress(host).dynamicPort()

  def instrumentWithMockServer(instrumentation: WireMockServer => Unit): Seq[Span] = {
    val mockServer = new WireMockServer(config)
    mockServer.start()
    WireMock.configureFor(host, mockServer.port())
    mockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/")).willReturn(WireMock.aResponse().withStatus(200)))

    instrumentation(mockServer)
    Thread.sleep(10000L) // Wait for sending spans from the exporter

    try {
      mockServer
        .findAll(RequestPatternBuilder.allRequests())
        .asScala
        .flatMap(req => ExportTraceServiceRequest.parseFrom(req.getBody).getResourceSpansList.asScala)
        .flatMap(_.getScopeSpansList.asScala)
        .flatMap(_.getSpansList.asScala)
        .toSeq
    } finally mockServer.stop()
  }
}

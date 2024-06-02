package dev.nomadblacky.scalatest_otel_reporter

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.BatchSpanProcessor
import io.opentelemetry.semconv.ResourceAttributes

import java.util.concurrent.TimeUnit

class WireMockOTelTestReporter(host: String, port: Int) extends OpenTelemetryTestReporter {
  def otel: OpenTelemetry = {
    // Export traces to the WireMock server over OTLP
    val wireMockOtlpExporter =
      OtlpHttpSpanExporter.builder
        .setEndpoint(s"http://$host:$port")
        .setTimeout(30, TimeUnit.SECONDS)
        .build

    val serviceNameResource =
      Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "scalatest-otel-wiremock"))

    // Set to process the spans by the WireMock Exporter
    val tracerProvider = SdkTracerProvider.builder
      .addSpanProcessor(BatchSpanProcessor.builder(wireMockOtlpExporter).build)
      .setResource(Resource.getDefault.merge(serviceNameResource))
      .build

    OpenTelemetrySdk.builder.setTracerProvider(tracerProvider).build
  }
}

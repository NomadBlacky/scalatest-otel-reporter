package dev.nomadblacky.scalatest_otel_reporter.examples

import dev.nomadblacky.scalatest_otel_reporter.BaseOpenTelemetryTestReporter
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.BatchSpanProcessor
import io.opentelemetry.semconv.ResourceAttributes

import java.util.concurrent.TimeUnit

class JaegerTestReporter extends BaseOpenTelemetryTestReporter {
  def otel: OpenTelemetry = {
    // Export traces to Jaeger over OTLP
    val jaegerOtlpExporter =
      OtlpGrpcSpanExporter.builder.setEndpoint("http://localhost:4317").setTimeout(30, TimeUnit.SECONDS).build

    val serviceNameResource =
      Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "scalatest-otel-demo-manual"))

    // Set to process the spans by the Jaeger Exporter
    val tracerProvider = SdkTracerProvider.builder
      .addSpanProcessor(BatchSpanProcessor.builder(jaegerOtlpExporter).build)
      .setResource(Resource.getDefault.merge(serviceNameResource))
      .build

    OpenTelemetrySdk.builder.setTracerProvider(tracerProvider).build
  }
}

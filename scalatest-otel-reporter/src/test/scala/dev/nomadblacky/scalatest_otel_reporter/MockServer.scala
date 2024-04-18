package dev.nomadblacky.scalatest_otel_reporter

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

trait MockServer extends BeforeAndAfterEach with BeforeAndAfterAll { self: Suite =>

  def host: String    = "localhost"
  def port: Int       = mockServer.port()
  def baseUrl: String = s"http://$host:${mockServer.port().toString}"

  def config: WireMockConfiguration = wireMockConfig().bindAddress(host).dynamicPort()

  lazy val mockServer: WireMockServer = {
    val server = new WireMockServer(config)
    server.start()
    WireMock.configureFor(host, server.port())
    server
  }

  override def beforeAll(): Unit = {
    val _ = mockServer
    super.beforeAll()
  }

  override def afterEach(): Unit = {
    mockServer.resetAll()
    super.afterEach()
  }

  override def afterAll(): Unit = {
    mockServer.stop()
    super.afterAll()
  }
}

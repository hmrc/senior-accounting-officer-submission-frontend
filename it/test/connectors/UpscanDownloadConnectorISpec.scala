/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import connectors.UpscanDownloadConnectorISpec.*
import play.api.http.HeaderNames
import play.api.http.Status.*
import support.ISpecBase
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

class UpscanDownloadConnectorISpec extends ISpecBase {

  lazy val SUT: UpscanDownloadConnector = app.injector.instanceOf[UpscanDownloadConnector]
  given HeaderCarrier                   = HeaderCarrier()

  override def additionalConfigs: Map[String, Any] = Map(
    "upscan-download.host" -> wireMockBaseUrlAsString
  )

  "A GET call from UpscanDownloadConnector.download to the target URL" must {
    "return a HttpResponse when the response status is 200" in {
      mockUpscanDownload(targetUrl = testUrl, status = OK, body = testBody)

      val result: HttpResponse = SUT.download(testUrl).futureValue

      result.status mustBe OK
      result.body mustBe testBody

      verify(
        1,
        getRequestedFor(urlEqualTo(testUrl))
          .withHeader(HeaderNames.USER_AGENT, equalTo("senior-accounting-officer-submission-frontend"))
      )
    }

    "return a HttpResponse when the response status is 4xx" in {
      mockUpscanDownload(targetUrl = testUrl, status = BAD_REQUEST, body = testBody)

      val result: HttpResponse = SUT.download(testUrl).futureValue

      result.status mustBe BAD_REQUEST
      result.body mustBe testBody
    }

    "return a HttpResponse when the response status is 5xx" in {
      mockUpscanDownload(targetUrl = testUrl, status = INTERNAL_SERVER_ERROR, body = testBody)

      val result: HttpResponse = SUT.download(testUrl).futureValue

      result.status mustBe INTERNAL_SERVER_ERROR
      result.body mustBe testBody
    }
  }
}

object UpscanDownloadConnectorISpec {

  val testUrl  = "/test/download/url"
  val testBody = "{}"

  import com.github.tomakehurst.wiremock.client.WireMock.*

  def mockUpscanDownload(targetUrl: String, status: Int, body: String): Unit =
    stubFor(
      get(urlEqualTo(targetUrl))
        .willReturn(
          aResponse()
            .withHeader("content-type", "application/json")
            .withBody(body)
            .withStatus(status)
        )
    )
}

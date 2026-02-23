/*
 * Copyright 2025 HM Revenue & Customs
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
import connectors.UpscanInitiateConnectorISpec.*
import models.{UpscanFileReference, UpscanInitiateRequestV2, UpscanInitiateResponse}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest
import support.ISpecBase
import uk.gov.hmrc.http.HeaderCarrier

class UpscanInitiateConnectorISpec extends ISpecBase {

  override def additionalConfigs: Map[String, Any] = Map(
    "microservice.services.upscan-initiate.port" -> wireMockPort
  )

  given HeaderCarrier = HeaderCarrier()

  lazy val SUT = app.injector.instanceOf[UpscanInitiateConnector]

  given Request[?] = FakeRequest()

  "UpscanInitiateConnector.initiateV2" must {
    "successfully initiate an upscan v2 upload" in {

      stubFor(
        post(urlEqualTo("/upscan/v2/initiate"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(Json.toJson(fakeUpscanInitiateResponse).toString)
          )
      )

      val response: UpscanInitiateResponse = SUT.initiateV2().futureValue

      Json.toJson(response) mustBe Json.toJson(fakeUpscanInitiateResponse)

      verify(
        1,
        postRequestedFor(urlEqualTo("/upscan/v2/initiate"))
          .withHeader(HeaderNames.USER_AGENT, equalTo("senior-accounting-officer-submission-frontend"))
          .withRequestBody(equalToJson(Json.toJson(expectedRequest).toString))
      )
    }

    "fail to initiate when upscan returns an error" in {
      stubFor(
        post(urlEqualTo("/upscan/v2/initiate"))
          .willReturn(
            aResponse()
              .withStatus(500)
          )
      )
      val response = SUT.initiateV2().failed.futureValue
      response mustBe a[uk.gov.hmrc.http.UpstreamErrorResponse]
    }
  }

}

object UpscanInitiateConnectorISpec {
  val fakeUpscanInitiateResponse = UpscanInitiateResponse(
    fileReference = UpscanFileReference("foo"),
    postTarget = "bar",
    formFields = Map("T1" -> "V1")
  )
  val expectedRequest = UpscanInitiateRequestV2(
    callbackUrl = "http://localhost:10058/internal/upscan-callback",
    successRedirect = Some(
      s"http://localhost:10058/senior-accounting-officer/submission/notification/upload/success"
    ),
    errorRedirect = Some("http://localhost:10058/senior-accounting-officer/submission/notification/upload/error")
  )
}

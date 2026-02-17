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
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import connectors.UpscanInitiateConnectorISpec.*
import models.{UpscanFileReference, UpscanInitiateResponse}
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest
import support.ISpecBase
import uk.gov.hmrc.http.HeaderCarrier

class UpscanInitiateConnectorISpec extends ISpecBase {

  override def additionalConfigs: Map[String, Any] = Map(
    "microservice.services.upscan-initiate.port" -> wireMockPort,
    "microservice.services.senior-accounting-officer-submission-frontend.port" -> wireMockPort
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
      
      val response: UpscanInitiateResponse = SUT.initiateV2(fakeUploadId).futureValue

      Json.toJson(response) mustBe Json.toJson(fakeUpscanInitiateResponse)
      
      verify(
        1,
        postRequestedFor(urlEqualTo("/upscan/v2/initiate"))
          .withRequestBody(matchingJsonPath("$.callbackUrl", containing("/upscan-callback")))
          .withRequestBody(matchingJsonPath("$.successRedirect", containing(s"/upload/success?uploadId=$fakeUploadId")))
          .withRequestBody(matchingJsonPath("$.errorRedirect", containing(s"/upload/error")))
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
      val response = SUT.initiateV2(fakeUploadId).failed.futureValue
      response mustBe a[uk.gov.hmrc.http.UpstreamErrorResponse]
    }
  }

}

object UpscanInitiateConnectorISpec {
  val fakeUploadId = "12345678"
  val fakeUpscanInitiateResponse= UpscanInitiateResponse(
    fileReference = UpscanFileReference("foo"),
    postTarget = "bar",
    formFields = Map("T1" -> "V1")
  )
}

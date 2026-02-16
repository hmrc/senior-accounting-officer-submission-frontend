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

  val SUT = app.injector.instanceOf[UpscanInitiateConnector]

  given Request[?] = FakeRequest()

  "UpscanInitiateConnector.initiateV2 must" - {

    val fakeUpscanInitiateResponse = UpscanInitiateResponse(
      fileReference = UpscanFileReference("foo"),
      postTarget = "bar",
      formFields = Map("T1" -> "V1")
    )
    
    "hit the correct endpoint" in {
      initiateStart()
      
      val response: UpscanInitiateResponse = SUT.initiateV2("01234dsfsdfsd")(using HeaderCarrier()).futureValue

      response mustBe UpscanInitiateResponse
      //      verify(
      //        1,
      //        postRequestedFor(urlEqualTo("/grs-stub/start"))
      //          .withRequestBody(equalTo(Json.toJson(testInput).toString))
      //      )

    }
  }

}

object UpscanInitiateConnectorISpec {

//  def fR= UpscanInitiateResponse(
//    fileReference = UpscanFileReference("foo"),
//    postTarget = "bar",
//    formFields = Map("T1" -> "V1")
//  )
  
  def initiateStart(): StubMapping = 
    stubFor(
      post(urlEqualTo("/upscan/v2/initiate"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(Json.toJson(fakeUpscanInitiateResponse).toString)
        )
    )

}

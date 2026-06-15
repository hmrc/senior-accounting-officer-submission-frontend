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
import connectors.InternalAuthTestOnlyConnectorISpec.*
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers.CREATED
import support.ISpecBase
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

class InternalAuthTestOnlyConnectorISpec extends ISpecBase {

  override def additionalConfigs: Map[String, Any] = Map(
    "microservice.services.internal-auth.port" -> wireMockPort
  )

  given HeaderCarrier = HeaderCarrier()

  lazy val SUT: InternalAuthTestOnlyConnector = app.injector.instanceOf[InternalAuthTestOnlyConnector]

  "InternalAuthTestOnlyConnector.grantSaoObjectStoreAccess" must {
    "configure the local internal auth token for SAO object-store access" in {
      stubFor(
        post(urlEqualTo("/test-only/token"))
          .willReturn(aResponse().withStatus(CREATED))
      )

      val response: HttpResponse = SUT.grantSaoObjectStoreAccess().futureValue

      response.status mustBe CREATED

      verify(
        1,
        postRequestedFor(urlEqualTo("/test-only/token"))
          .withHeader(HeaderNames.USER_AGENT, equalTo("senior-accounting-officer-submission-frontend"))
          .withRequestBody(equalToJson(expectedRequest.toString))
      )
    }
  }
}

object InternalAuthTestOnlyConnectorISpec {
  val expectedRequest = Json.obj(
    "token" -> "1234",
    "principal" -> "senior-accounting-officer-submission-frontend",
    "permissions" -> Json.arr(
      Json.obj(
        "resourceType" -> "object-store",
        "resourceLocation" -> "senior-accounting-officer",
        "actions" -> Json.arr("READ", "WRITE")
      )
    )
  )
}

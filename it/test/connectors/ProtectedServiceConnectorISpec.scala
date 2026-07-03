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
import connectors.ProtectedServiceConnectorISpec.*
import java.net.URI
import models.notification.NotificationRequest
import play.api.http.Status.CREATED
import support.ISpecBase
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpResponse
import models.notification.Company
import models.notification.Sao
import play.api.libs.json.Json

class ProtectedServiceConnectorISpec extends ISpecBase {

  override def additionalConfigs: Map[String, Any] = Map(
    "microservice.services.protected-service.port" -> wireMockPort
  )

  lazy val SUT: ProtectedServiceConnector = app.injector.instanceOf[ProtectedServiceConnector]
  given HeaderCarrier                     = HeaderCarrier()

  def testUrl = "/senior-accounting-officer/notification"

  "A POST call from ProtectedServiceConnector.submit to the target URL" must {
    "return a HttpResponse when the response status is 201" in {
      stubFor(
        post(urlEqualTo(testUrl))
          .willReturn(
            aResponse()
              .withHeader("content-type", "application/json")
              .withBody(testBody)
              .withStatus(CREATED)
          )
      )

      val result: HttpResponse =
        SUT
          .postNotification(
            NotificationRequest(
              subscriptionId = subscriptionId,
              companies = List(
                Company(
                  crn = None,
                  utr = "0000000000",
                  name = "String",
                  accPeriodEnd = "2000-01-01",
                  status = "String",
                  `type` = "LTD"
                )
              ),
              saos = List(
                Sao(
                  name = "String",
                  fromDate = None,
                  email = None,
                  toDate = None
                )
              ),
              remarks = None
            )
          )
          .futureValue

      result.status mustBe CREATED
      result.body mustBe testBody

      verify(
        1,
        postRequestedFor(urlEqualTo(URI(testUrl).getPath))
      )
    }
  }
}

object ProtectedServiceConnectorISpec {
  val testBody       = Json.obj("notificationRef" -> "NOT0123456789").toString
  val subscriptionId = "123"
}

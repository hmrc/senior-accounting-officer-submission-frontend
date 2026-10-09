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
import connectors.CertificateSubmissionConnectorISpec.{testBody, testBodyFaultTolerance}
import models.certificate.{CertificateFaultToleranceResponse, CertificateSubmissionRequest, CertificateSubmissionResponse}
import play.api.http.Status.{ACCEPTED, CREATED, NO_CONTENT}
import play.api.libs.json.Json
import support.ISpecBase
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.net.URI

class CertificateSubmissionConnectorISpec extends ISpecBase {

  override def additionalConfigs: Map[String, Any] = Map(
    "microservice.services.senior-accounting-officer.port" -> wireMockPort
  )

  lazy val SUT: CertificateSubmissionConnector = app.injector.instanceOf[CertificateSubmissionConnector]
  given HeaderCarrier                          = HeaderCarrier()

  def submitTestUrl = "/senior-accounting-officer/certificate"
  def submitFaultToleranceTestUrl = "/senior-accounting-officer/v2/certificate"
  def stateOfWorkItemTestUrl = "/senior-accounting-officer/v2/certificate/key"

  "A POST call from CertificateSubmissionConnector.submit to the target URL" must {
    "return a HttpResponse when the response status is 201" in {
      stubFor(
        post(urlEqualTo(submitTestUrl))
          .willReturn(
            aResponse()
              .withHeader("content-type", "application/json")
              .withBody(testBody)
              .withStatus(CREATED)
          )
      )

      val company: CertificateSubmissionCompany =
        CertificateSubmissionCompany(
          crn = Some("AB123456"),
          utr = "1234567890",
          name = "Example Ltd",
          accPeriodEnd = "2026-03-31",
          status = CompanyStatus.Active,
          `type` = CompanyType.LTD,
          isCorporationTaxQualified = true,
          isVatQualified = false,
          isPayeQualified = false,
          isInsurancePremiumTaxQualified = false,
          isStampDutyLandTaxQualified = false,
          isStampDutyReserveTaxQualified = false,
          isPetroleumRevenueTaxQualified = false,
          isCustomsDutiesQualified = false,
          isExciseDutiesQualified = false,
          isBankLevyQualified = false,
          qualificationStatement = Some("Test Statement")
        )

      val result: CertificateSubmissionResponse =
        SUT
          .submit(CertificateSubmissionRequest(
            submitterName = Some("Proxy Person"),
            saoName = "Senior Officer",
            saoDeclarationName = "Senior Officer",
            saoEmail = "sao@example.com",
            companies = Seq(company),
            remarks = Some("Certificate remarks"),
            idempotencyKey = None
          )
          )
          .futureValue

      result mustBe CertificateSubmissionResponse("NOT0123456789")

      verify(
        1,
        postRequestedFor(urlEqualTo(URI(submitTestUrl).getPath))
      )
    }
  }


  "A POST call from CertificateSubmissionConnector.postNotificationWithFaultTolerance to the target URL" must {
    "return a HttpResponse when the response status is 202" in {
      stubFor(
        post(urlEqualTo(submitFaultToleranceTestUrl))
          .willReturn(
            aResponse()
              .withHeader("content-type", "application/json")
              .withBody(testBodyFaultTolerance)
              .withStatus(ACCEPTED)
          )
      )

      val result: CertificateFaultToleranceResponse =
        SUT
          .submitWithFaultTolerance(
            CertificateSubmissionRequest(
              submitterName = Some("Proxy Person"),
              saoName = "Senior Officer",
              saoDeclarationName = "Senior Officer",
              saoEmail = "sao@example.com",
              companies = Seq(company),
              remarks = Some("Certificate remarks"),
              idempotencyKey = Some("key")
            )
          )
          .futureValue

      result mustBe CertificateFaultToleranceResponse("key")

      verify(
        1,
        postRequestedFor(urlEqualTo(URI(submitFaultToleranceTestUrl).getPath))
      )
    }
  }


  "A GET call from CertificateSubmissionConnector.getStateOfWorkItem to the target URL" must {
    "return a HttpResponse when the response status is 204" in {
      stubFor(
        get(urlEqualTo(stateOfWorkItemTestUrl))
          .willReturn(
            aResponse()
              .withStatus(NO_CONTENT)
          )
      )

      val result: HttpResponse =
        SUT
          .getStateOfWorkItem("key")
          .futureValue

      result.status mustBe NO_CONTENT

      verify(
        1,
        getRequestedFor(urlEqualTo(URI(stateOfWorkItemTestUrl).getPath))
      )
    }
  }
}

object CertificateSubmissionConnectorISpec {
  val testBody: String = Json.obj("certificateRef" -> "NOT0123456789").toString
  val testBodyFaultTolerance: String = Json.obj("idempotencyKey" -> "key").toString
}

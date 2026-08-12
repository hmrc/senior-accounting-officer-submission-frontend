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

package services

import base.SpecBase
import connectors.CertificateSubmissionConnector
import models.UserAnswers
import models.certificate.*
import models.upload.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.certificate.*
import play.api.libs.json.Json
import repositories.SessionRepository
import services.CertificateSubmissionService.CertificateSubmissionResult
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

import java.time.LocalDate

import CertificateSubmissionServiceSpec.*

class CertificateSubmissionServiceSpec extends SpecBase {

  given HeaderCarrier    = HeaderCarrier()
  given ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  "CertificateSubmissionService" - {

    "must build the certificate payload, call the connector, wipe the Mongo data, and return the certificate ref" in {
      val connector         = mock[CertificateSubmissionConnector]
      val sessionRepository = mock[SessionRepository]
      val requestCaptor     = ArgumentCaptor.forClass(classOf[CertificateSubmissionRequest])

      when(sessionRepository.claimCertificateSubmissionToken(eqTo(userAnswersId), eqTo("token")))
        .thenReturn(Future.successful(true))
      when(connector.submit(any())(using any()))
        .thenReturn(Future.successful(CertificateSubmissionResponse("CRT0123456789")))
      when(sessionRepository.set(any())).thenReturn(Future.successful(true))

      val service = new CertificateSubmissionService(connector, sessionRepository)

      val result = service
        .submit(userAnswersId, completeUserAnswers(emptyUserAnswers), "token")
        .futureValue

      result mustBe CertificateSubmissionResult.Submitted("CRT0123456789")
      verify(connector).submit(requestCaptor.capture())(using any())

      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(sessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.id mustBe userAnswersId
      userAnswersCaptor.getValue.data mustBe Json.obj()
      verify(sessionRepository, never()).clear(any())

      val request = requestCaptor.getValue
      request.submitterName.value mustBe "Proxy Person"
      request.saoName mustBe "Senior Officer"
      request.saoEmail mustBe "sao@example.com"
      request.remarks.value mustBe "Certificate remarks"

      val company = request.companies.head
      company.crn.value mustBe "AB123456"
      company.utr mustBe "1234567890"
      company.name mustBe "Example Ltd"
      company.accPeriodEnd mustBe "2026-03-31"
      company.status mustBe "ACTIVE"
      company.`type` mustBe "LTD"
      company.isCorporationTaxQualified mustBe true
      company.isVatQualified mustBe true
      company.isPayeQualified mustBe true
      company.isInsurancePremiumTaxQualified mustBe true
      company.isStampDutyLandTaxQualified mustBe true
      company.isStampDutyReserveTaxQualified mustBe true
      company.isPetroleumRevenueTaxQualified mustBe true
      company.isCustomsDutiesQualified mustBe true
      company.isExciseDutiesQualified mustBe true
      company.isBankLevyQualified mustBe true
    }

    "must not call the connector when the token has already been used" in {
      val connector         = mock[CertificateSubmissionConnector]
      val sessionRepository = mock[SessionRepository]

      when(sessionRepository.claimCertificateSubmissionToken(eqTo(userAnswersId), eqTo("token")))
        .thenReturn(Future.successful(false))

      val service = new CertificateSubmissionService(connector, sessionRepository)

      val result = service
        .submit(userAnswersId, completeUserAnswers(emptyUserAnswers), "token")
        .futureValue

      result mustBe CertificateSubmissionResult.Duplicate
      verify(connector, never()).submit(any())(using any())
      verify(sessionRepository, never()).set(any())
    }

    "must retain Mongo data when the connector fails" in {
      val connector         = mock[CertificateSubmissionConnector]
      val sessionRepository = mock[SessionRepository]

      when(sessionRepository.claimCertificateSubmissionToken(eqTo(userAnswersId), eqTo("token")))
        .thenReturn(Future.successful(true))
      when(connector.submit(any())(using any()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val service = new CertificateSubmissionService(connector, sessionRepository)

      val result = service
        .submit(userAnswersId, completeUserAnswers(emptyUserAnswers), "token")
        .futureValue

      result mustBe CertificateSubmissionResult.Failed
      verify(sessionRepository, never()).set(any())
    }

    "must still succeed when the certificate is submitted but wiping the session data fails" in {
      val connector         = mock[CertificateSubmissionConnector]
      val sessionRepository = mock[SessionRepository]

      when(sessionRepository.claimCertificateSubmissionToken(eqTo(userAnswersId), eqTo("token")))
        .thenReturn(Future.successful(true))
      when(connector.submit(any())(using any()))
        .thenReturn(Future.successful(CertificateSubmissionResponse("CRT0123456789")))
      when(sessionRepository.set(any())).thenReturn(Future.failed(new RuntimeException("mongo down")))

      val service = new CertificateSubmissionService(connector, sessionRepository)

      val result = service
        .submit(userAnswersId, completeUserAnswers(emptyUserAnswers), "token")
        .futureValue

      result mustBe CertificateSubmissionResult.Submitted("CRT0123456789")
      verify(sessionRepository).set(any())
    }

    "must return MissingData when required answers are absent" in {
      val connector         = mock[CertificateSubmissionConnector]
      val sessionRepository = mock[SessionRepository]
      val service           = new CertificateSubmissionService(connector, sessionRepository)

      val result = service
        .submit(userAnswersId, emptyUserAnswers, "token")
        .futureValue

      result mustBe CertificateSubmissionResult.MissingData
      verify(sessionRepository, never()).claimCertificateSubmissionToken(any(), any())
      verify(connector, never()).submit(any())(using any())
    }
  }
}

object CertificateSubmissionServiceSpec {

  def completeUserAnswers(emptyUserAnswers: UserAnswers): UserAnswers =
    emptyUserAnswers
      .set(CertificateSaoFullNamePage, "Senior Officer")
      .success
      .value
      .set(CertificateSaoEmailPage, "sao@example.com")
      .success
      .value
      .set(CertificateWhoIsSubmittingPage, CertificateWhoIsSubmitting.StandIn)
      .success
      .value
      .set(CertificateDeclarationStandInPage, CertificateDeclarationStandIn("Proxy Person", "Senior Officer"))
      .success
      .value
      .set(CertificateAdditionalInformationPage, Some("Certificate remarks"))
      .success
      .value
      .set(CertificateUploadTemplateTablePage, UploadTemplateTableData(Seq(parsedRow), Seq.empty))
      .success
      .value

  val parsedRow: ParsedSubmissionRow =
    ParsedSubmissionRow(
      notification = NotificationFields(
        companyName = "Example Ltd",
        companyUtr = CompanyUtr("1234567890"),
        companyCrn = Some(CompanyCrn("AB123456")),
        companyType = CompanyType.LTD,
        companyStatus = CompanyStatus.Active,
        financialYearEndDate = LocalDate.of(2026, 3, 31)
      ),
      certificate = CertificateFields(
        corporationTax = true,
        valueAddedTax = true,
        paye = true,
        insurancePremiumTax = true,
        stampDutyLandTax = true,
        stampDutyReserveTax = true,
        petroleumRevenueTax = true,
        customsDuties = true,
        exciseDuties = true,
        bankLevy = true,
        certificateType = Some(CertificateType.Qualified),
        additionalInformation = None
      )
    )
}

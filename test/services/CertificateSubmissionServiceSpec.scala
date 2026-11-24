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
import models.NormalMode
import models.UserAnswers
import models.certificate.*
import models.upload.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.QuestionPage
import pages.certificate.*
import pages.notification.UploadTemplateTablePage
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.{JsObject, Json}
import repositories.SessionRepository
import services.CertificateSubmissionService.CertificateSubmissionResult
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.{ExecutionContext, Future}

import java.time.LocalDate

import CertificateSubmissionServiceSpec.*

class CertificateSubmissionServiceSpec extends SpecBase {

  given HeaderCarrier = HeaderCarrier()

  given ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  Seq(true, false).foreach { isCombinedJourney =>
    s"CertificateSubmissionService.submit - isCombinedJourney = $isCombinedJourney" - {

      "must build the certificate payload for sao, call the connector, wipe the Mongo data, and return the certificate ref" in {
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
          .submit(userAnswersId, saoUserAnswers(isCombinedJourney), "token", isCombinedJourney = isCombinedJourney)
          .futureValue

        result mustBe CertificateSubmissionResult.Submitted("CRT0123456789")
        verify(connector).submit(requestCaptor.capture())(using any())

        val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(sessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.id mustBe userAnswersId
        userAnswersCaptor.getValue.data mustBe Json.obj()
        verify(sessionRepository, never()).clear(any())

        val request = requestCaptor.getValue
        request.saoName mustBe "Senior Officer"
        request.saoDeclarationName mustBe "SAO Declaration Signatory"
        request.saoEmail mustBe "sao@example.com"
        request.remarks.value mustBe "Certificate remarks"

        val company = request.companies.head
        company.crn.value mustBe "AB123456"
        company.utr mustBe "1234567890"
        company.name mustBe "Example Ltd"
        company.accPeriodEnd mustBe "2026-03-31"
        company.status mustBe CompanyStatus.Active
        company.`type` mustBe CompanyType.LTD
        company.isCorporationTaxQualified mustBe true
        company.isVatQualified mustBe false
        company.isPayeQualified mustBe false
        company.isInsurancePremiumTaxQualified mustBe false
        company.isStampDutyLandTaxQualified mustBe false
        company.isStampDutyReserveTaxQualified mustBe false
        company.isPetroleumRevenueTaxQualified mustBe false
        company.isCustomsDutiesQualified mustBe false
        company.isExciseDutiesQualified mustBe false
        company.isBankLevyQualified mustBe false
      }

      "must build the certificate payload for standIn, call the connector, wipe the Mongo data, and return the certificate ref" in {
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
          .submit(userAnswersId, standInUserAnswers(isCombinedJourney), "token", isCombinedJourney = isCombinedJourney)
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
        request.saoDeclarationName mustBe "Declaration Officer"
        request.saoEmail mustBe "sao@example.com"
        request.remarks.value mustBe "Certificate remarks"

        val company = request.companies.head
        company.crn.value mustBe "AB123456"
        company.utr mustBe "1234567890"
        company.name mustBe "Example Ltd"
        company.accPeriodEnd mustBe "2026-03-31"
        company.status mustBe CompanyStatus.Active
        company.`type` mustBe CompanyType.LTD
        company.isCorporationTaxQualified mustBe true
        company.isVatQualified mustBe false
        company.isPayeQualified mustBe false
        company.isInsurancePremiumTaxQualified mustBe false
        company.isStampDutyLandTaxQualified mustBe false
        company.isStampDutyReserveTaxQualified mustBe false
        company.isPetroleumRevenueTaxQualified mustBe false
        company.isCustomsDutiesQualified mustBe false
        company.isExciseDutiesQualified mustBe false
        company.isBankLevyQualified mustBe false
      }

      "must send the SAO declaration name separately when the SAO submits" in {
        val connector         = mock[CertificateSubmissionConnector]
        val sessionRepository = mock[SessionRepository]
        val requestCaptor     = ArgumentCaptor.forClass(classOf[CertificateSubmissionRequest])
        val answers           = standInUserAnswers(isCombinedJourney)
          .set(CertificateWhoIsSubmittingPage(NormalMode), CertificateWhoIsSubmitting.Sao)
          .success
          .value
          .set(CertificateDeclarationSaoPage(NormalMode), "SAO Declaration Signatory")
          .success
          .value

        when(sessionRepository.claimCertificateSubmissionToken(eqTo(userAnswersId), eqTo("token")))
          .thenReturn(Future.successful(true))
        when(connector.submit(any())(using any()))
          .thenReturn(Future.successful(CertificateSubmissionResponse("CRT0123456789")))
        when(sessionRepository.set(any())).thenReturn(Future.successful(true))

        val service = new CertificateSubmissionService(connector, sessionRepository)

        service.submit(userAnswersId, answers, "token", isCombinedJourney = isCombinedJourney).futureValue mustBe
          CertificateSubmissionResult.Submitted("CRT0123456789")
        verify(connector).submit(requestCaptor.capture())(using any())
        requestCaptor.getValue.saoName mustBe "Senior Officer"
        requestCaptor.getValue.saoDeclarationName mustBe "SAO Declaration Signatory"
        requestCaptor.getValue.submitterName mustBe None
      }

      for submitter <- CertificateWhoIsSubmitting.values yield {
        s"must not submit or claim the token when the $submitter declaration is missing" in {
          val connector         = mock[CertificateSubmissionConnector]
          val sessionRepository = mock[SessionRepository]
          val answers           = standInUserAnswers(isCombinedJourney)
            .set(CertificateWhoIsSubmittingPage(NormalMode), submitter)
            .success
            .value
            .remove(CertificateDeclarationStandInPage(NormalMode))
            .success
            .value
          val service = new CertificateSubmissionService(connector, sessionRepository)

          service
            .submit(userAnswersId, answers, "token", isCombinedJourney = isCombinedJourney)
            .futureValue mustBe CertificateSubmissionResult.MissingData
          verify(sessionRepository, never()).claimCertificateSubmissionToken(any(), any())
          verify(connector, never()).submit(any())(using any())
        }
      }

      "must not call the connector when the token has already been used" in {
        val connector         = mock[CertificateSubmissionConnector]
        val sessionRepository = mock[SessionRepository]

        when(sessionRepository.claimCertificateSubmissionToken(eqTo(userAnswersId), eqTo("token")))
          .thenReturn(Future.successful(false))

        val service = new CertificateSubmissionService(connector, sessionRepository)

        val result = service
          .submit(userAnswersId, standInUserAnswers(isCombinedJourney), "token", isCombinedJourney = isCombinedJourney)
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
          .submit(userAnswersId, standInUserAnswers(isCombinedJourney), "token", isCombinedJourney = isCombinedJourney)
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
          .submit(userAnswersId, standInUserAnswers(isCombinedJourney), "token", isCombinedJourney = isCombinedJourney)
          .futureValue

        result mustBe CertificateSubmissionResult.Submitted("CRT0123456789")
        verify(sessionRepository).set(any())
      }

      "must return MissingData when required answers are absent" in {
        val connector         = mock[CertificateSubmissionConnector]
        val sessionRepository = mock[SessionRepository]
        val service           = new CertificateSubmissionService(connector, sessionRepository)

        val result = service
          .submit(userAnswersId, emptyUserAnswers, "token", isCombinedJourney = isCombinedJourney)
          .futureValue

        result mustBe CertificateSubmissionResult.MissingData
        verify(sessionRepository, never()).claimCertificateSubmissionToken(any(), any())
        verify(connector, never()).submit(any())(using any())
      }
    }
  }
  Seq(true, false).foreach { isCombinedJourney =>
    s"CertificateSubmissionService.submitWithFaultTolerance - isCombinedJourney = $isCombinedJourney " - {
      "must build the certificate payload for sao, call the connector, and return the certificate ref" in {
        val connector         = mock[CertificateSubmissionConnector]
        val sessionRepository = mock[SessionRepository]
        val requestCaptor     = ArgumentCaptor.forClass(classOf[CertificateSubmissionRequest])

        when(sessionRepository.claimCertificateSubmissionToken(eqTo(userAnswersId), eqTo("token")))
          .thenReturn(Future.successful(true))
        when(connector.submitWithFaultTolerance(any())(using any()))
          .thenReturn(Future.successful(CertificateFaultToleranceResponse("key")))
        when(sessionRepository.set(any())).thenReturn(Future.successful(true))

        val service = new CertificateSubmissionService(connector, sessionRepository)

        val result = service
          .submitWithFaultTolerance(
            userAnswersId,
            saoUserAnswers(isCombinedJourney),
            "token",
            isCombinedJourney = isCombinedJourney
          )
          .futureValue

        result mustBe CertificateSubmissionResult.Pending("key")
        verify(connector).submitWithFaultTolerance(requestCaptor.capture())(using any())

        val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(sessionRepository).set(userAnswersCaptor.capture())
        val capturedCertificate = (userAnswersCaptor.getValue.data \ "certificate").as[JsObject]
        userAnswersCaptor.getValue.id mustBe userAnswersId
        (capturedCertificate \ "certificateIdempotencyKey").asOpt[String] must not be empty
        (capturedCertificate - "certificateIdempotencyKey") mustBe (saoUserAnswers(
          isCombinedJourney
        ).data \ "certificate").as[JsObject]
        verify(sessionRepository, never()).clear(any())

        val request = requestCaptor.getValue
        request.saoName mustBe "Senior Officer"
        request.saoDeclarationName mustBe "SAO Declaration Signatory"
        request.saoEmail mustBe "sao@example.com"
        request.remarks.value mustBe "Certificate remarks"

        val company = request.companies.head
        company.crn.value mustBe "AB123456"
        company.utr mustBe "1234567890"
        company.name mustBe "Example Ltd"
        company.accPeriodEnd mustBe "2026-03-31"
        company.status mustBe CompanyStatus.Active
        company.`type` mustBe CompanyType.LTD
        company.isCorporationTaxQualified mustBe true
        company.isVatQualified mustBe false
        company.isPayeQualified mustBe false
        company.isInsurancePremiumTaxQualified mustBe false
        company.isStampDutyLandTaxQualified mustBe false
        company.isStampDutyReserveTaxQualified mustBe false
        company.isPetroleumRevenueTaxQualified mustBe false
        company.isCustomsDutiesQualified mustBe false
        company.isExciseDutiesQualified mustBe false
        company.isBankLevyQualified mustBe false
      }

      "must build the certificate payload for standIn, call the connector, and return the certificate ref" in {
        val connector         = mock[CertificateSubmissionConnector]
        val sessionRepository = mock[SessionRepository]
        val requestCaptor     = ArgumentCaptor.forClass(classOf[CertificateSubmissionRequest])

        when(sessionRepository.claimCertificateSubmissionToken(eqTo(userAnswersId), eqTo("token")))
          .thenReturn(Future.successful(true))
        when(connector.submitWithFaultTolerance(any())(using any()))
          .thenReturn(Future.successful(CertificateFaultToleranceResponse("key")))
        when(sessionRepository.set(any())).thenReturn(Future.successful(true))

        val service = new CertificateSubmissionService(connector, sessionRepository)

        val result = service
          .submitWithFaultTolerance(
            userAnswersId,
            standInUserAnswers(isCombinedJourney),
            "token",
            isCombinedJourney = isCombinedJourney
          )
          .futureValue

        result mustBe CertificateSubmissionResult.Pending("key")
        verify(connector).submitWithFaultTolerance(requestCaptor.capture())(using any())

        val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(sessionRepository).set(userAnswersCaptor.capture())
        val capturedCertificate = (userAnswersCaptor.getValue.data \ "certificate").as[JsObject]
        userAnswersCaptor.getValue.id mustBe userAnswersId
        (capturedCertificate \ "certificateIdempotencyKey").asOpt[String] must not be empty
        (capturedCertificate - "certificateIdempotencyKey") mustBe (standInUserAnswers(
          isCombinedJourney
        ).data \ "certificate").as[JsObject]
        verify(sessionRepository, never()).clear(any())

        val request = requestCaptor.getValue
        request.submitterName.value mustBe "Proxy Person"
        request.saoName mustBe "Senior Officer"
        request.saoDeclarationName mustBe "Declaration Officer"
        request.saoEmail mustBe "sao@example.com"
        request.remarks.value mustBe "Certificate remarks"

        val company = request.companies.head
        company.crn.value mustBe "AB123456"
        company.utr mustBe "1234567890"
        company.name mustBe "Example Ltd"
        company.accPeriodEnd mustBe "2026-03-31"
        company.status mustBe CompanyStatus.Active
        company.`type` mustBe CompanyType.LTD
        company.isCorporationTaxQualified mustBe true
        company.isVatQualified mustBe false
        company.isPayeQualified mustBe false
        company.isInsurancePremiumTaxQualified mustBe false
        company.isStampDutyLandTaxQualified mustBe false
        company.isStampDutyReserveTaxQualified mustBe false
        company.isPetroleumRevenueTaxQualified mustBe false
        company.isCustomsDutiesQualified mustBe false
        company.isExciseDutiesQualified mustBe false
        company.isBankLevyQualified mustBe false
      }

      "must not call the connector when the token has already been used" in {
        val connector         = mock[CertificateSubmissionConnector]
        val sessionRepository = mock[SessionRepository]

        when(sessionRepository.claimCertificateSubmissionToken(eqTo(userAnswersId), eqTo("token")))
          .thenReturn(Future.successful(false))
        when(sessionRepository.set(any())).thenReturn(Future.successful(true))

        val service = new CertificateSubmissionService(connector, sessionRepository)

        val result = service
          .submitWithFaultTolerance(
            userAnswersId,
            standInUserAnswers(isCombinedJourney),
            "token",
            isCombinedJourney = isCombinedJourney
          )
          .futureValue

        result mustBe CertificateSubmissionResult.Duplicate
        verify(connector, never()).submitWithFaultTolerance(any())(using any())
        verify(sessionRepository, times(1)).set(any())
      }

      "must retain Mongo data when the connector fails" in {
        val connector         = mock[CertificateSubmissionConnector]
        val sessionRepository = mock[SessionRepository]

        when(sessionRepository.claimCertificateSubmissionToken(eqTo(userAnswersId), eqTo("token")))
          .thenReturn(Future.successful(true))
        when(connector.submitWithFaultTolerance(any())(using any()))
          .thenReturn(Future.failed(new RuntimeException("boom")))
        when(sessionRepository.set(any())).thenReturn(Future.successful(true))

        val service = new CertificateSubmissionService(connector, sessionRepository)

        val result = service
          .submitWithFaultTolerance(
            userAnswersId,
            standInUserAnswers(isCombinedJourney),
            "token",
            isCombinedJourney = isCombinedJourney
          )
          .futureValue

        result mustBe CertificateSubmissionResult.Failed
        verify(sessionRepository, times(1)).set(any())
      }

      "must still succeed when the certificate is submitted but wiping the session data fails" in {
        val connector         = mock[CertificateSubmissionConnector]
        val sessionRepository = mock[SessionRepository]

        when(sessionRepository.claimCertificateSubmissionToken(eqTo(userAnswersId), eqTo("token")))
          .thenReturn(Future.successful(true))
        when(connector.submitWithFaultTolerance(any())(using any()))
          .thenReturn(Future.successful(CertificateFaultToleranceResponse("key")))
        when(sessionRepository.set(any())).thenReturn(Future.failed(new RuntimeException("mongo down")))

        val service = new CertificateSubmissionService(connector, sessionRepository)

        val result = service
          .submitWithFaultTolerance(
            userAnswersId,
            standInUserAnswers(isCombinedJourney),
            "token",
            isCombinedJourney = isCombinedJourney
          )
          .futureValue

        result mustBe CertificateSubmissionResult.Pending("key")
        verify(sessionRepository).set(any())
      }

      "must return MissingData when required answers are absent" in {
        val connector         = mock[CertificateSubmissionConnector]
        val sessionRepository = mock[SessionRepository]
        val service           = new CertificateSubmissionService(connector, sessionRepository)

        when(sessionRepository.set(any())).thenReturn(Future.successful(true))

        val result = service
          .submitWithFaultTolerance(userAnswersId, emptyUserAnswers, "token", isCombinedJourney = isCombinedJourney)
          .futureValue

        result mustBe CertificateSubmissionResult.MissingData
        verify(sessionRepository, never()).claimCertificateSubmissionToken(any(), any())
        verify(connector, never()).submitWithFaultTolerance(any())(using any())
      }

      "must reuse the idempotency key if it's already been stored in mongo" in {
        val connector         = mock[CertificateSubmissionConnector]
        val sessionRepository = mock[SessionRepository]
        val requestCaptor     = ArgumentCaptor.forClass(classOf[CertificateSubmissionRequest])

        when(sessionRepository.claimCertificateSubmissionToken(eqTo(userAnswersId), eqTo("token")))
          .thenReturn(Future.successful(true))
        when(connector.submitWithFaultTolerance(any())(using any()))
          .thenReturn(Future.successful(CertificateFaultToleranceResponse("idemKey")))
        when(sessionRepository.set(any())).thenReturn(Future.successful(true))

        val service = new CertificateSubmissionService(connector, sessionRepository)

        def saoAnswersWithIdempotencyKey =
          saoUserAnswers(isCombinedJourney)
            .set(CertificateIdempotencyKey, Some("idemKey"))
            .success
            .value

        val result = service
          .submitWithFaultTolerance(
            userAnswersId,
            saoAnswersWithIdempotencyKey,
            "token",
            isCombinedJourney = isCombinedJourney
          )
          .futureValue

        result mustBe CertificateSubmissionResult.Pending("idemKey")
        verify(connector).submitWithFaultTolerance(requestCaptor.capture())(using any())
        verify(sessionRepository, never()).clear(any())
        verify(sessionRepository, never()).set(any())
      }
    }
  }

  "CertificateSubmissionService.getStateOfWorkItem" - {
    "must get the state of the work item and return the certificate ref if it's succeeded" in {
      val connector         = mock[CertificateSubmissionConnector]
      val sessionRepository = mock[SessionRepository]

      when(connector.getStateOfWorkItem(any())(using any()))
        .thenReturn(Future.successful(HttpResponse(status = OK, body = """{"certificateRef": "certificateRef"}""")))
      when(sessionRepository.set(any())).thenReturn(Future.successful(true))

      val service = new CertificateSubmissionService(connector, sessionRepository)

      val result = service
        .getStateOfWorkItem("key", emptyUserAnswers)
        .futureValue

      result mustBe CertificateSubmissionResult.Submitted("certificateRef")
      verify(connector).getStateOfWorkItem(any())(using any())
    }

    "must get the state of the work item and return a pending status if the endpoint returns no content" in {
      val connector         = mock[CertificateSubmissionConnector]
      val sessionRepository = mock[SessionRepository]

      when(connector.getStateOfWorkItem(any())(using any()))
        .thenReturn(Future.successful(HttpResponse(status = NO_CONTENT)))

      val service = new CertificateSubmissionService(connector, sessionRepository)

      val result = service
        .getStateOfWorkItem("key", emptyUserAnswers)
        .futureValue

      result mustBe CertificateSubmissionResult.Pending("key")
      verify(connector).getStateOfWorkItem(any())(using any())
    }

    "must return a failure if there is a failure while getting the state of work item" in {
      val connector         = mock[CertificateSubmissionConnector]
      val sessionRepository = mock[SessionRepository]

      when(connector.getStateOfWorkItem(any())(using any()))
        .thenReturn(Future.successful(HttpResponse(status = INTERNAL_SERVER_ERROR)))

      val service = new CertificateSubmissionService(connector, sessionRepository)

      val result = service
        .getStateOfWorkItem("key", emptyUserAnswers)
        .futureValue

      result mustBe CertificateSubmissionResult.Failed
      verify(connector).getStateOfWorkItem(any())(using any())
    }
  }
}

object CertificateSubmissionServiceSpec extends SpecBase {

  def tableDataPage(isCombinedJourney: Boolean): QuestionPage[UploadTemplateTableData] =
    if isCombinedJourney then UploadTemplateTablePage else CertificateUploadTemplateTablePage

  def saoUserAnswers(isCombinedJourney: Boolean): UserAnswers = {
    UserAnswers("id")
      .add(CertificateSaoFullNamePage, "Senior Officer")
      .add(CertificateSaoEmailPage, "sao@example.com")
      .add(CertificateWhoIsSubmittingPage(NormalMode), CertificateWhoIsSubmitting.Sao)
      .add(CertificateDeclarationSaoPage(NormalMode), "SAO Declaration Signatory")
      .add(CertificateAdditionalInformationPage, Some("Certificate remarks"))
      .add(tableDataPage(isCombinedJourney), UploadTemplateTableData(Seq(parsedRow), Seq.empty))
  }

  def standInUserAnswers(isCombinedJourney: Boolean): UserAnswers =
    UserAnswers("id")
      .add(CertificateSaoFullNamePage, "Senior Officer")
      .add(CertificateSaoEmailPage, "sao@example.com")
      .add(CertificateWhoIsSubmittingPage(NormalMode), CertificateWhoIsSubmitting.StandIn)
      .add(
        CertificateDeclarationStandInPage(NormalMode),
        CertificateDeclarationStandIn("Proxy Person", "Declaration Officer")
      )
      .add(CertificateAdditionalInformationPage, Some("Certificate remarks"))
      .add(tableDataPage(isCombinedJourney), UploadTemplateTableData(Seq(parsedRow), Seq.empty))

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
      certificate = Some(
        CertificateFields(
          corporationTax = true,
          valueAddedTax = false,
          paye = false,
          insurancePremiumTax = false,
          stampDutyLandTax = false,
          stampDutyReserveTax = false,
          petroleumRevenueTax = false,
          customsDuties = false,
          exciseDuties = false,
          bankLevy = false,
          certificateType = Some(CertificateType.Qualified),
          qualificationStatement = None
        )
      )
    )
}

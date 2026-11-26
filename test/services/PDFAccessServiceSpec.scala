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
import connectors.PDFAccessConnector
import models.SubmissionDocumentType
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.inject.bind
import play.api.test.Helpers.*
import services.PDFAccessServiceSpec.*
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class PDFAccessServiceSpec extends SpecBase {

  given HeaderCarrier = HeaderCarrier()

  private def streamedResponse(status: Int): HttpResponse =
    HttpResponse(
      status,
      Source.single(ByteString(pdfContent)),
      Map(
        "Content-Length" -> Seq(pdfContent.length.toString),
        "Content-Type"   -> Seq("application/pdf")
      )
    )

  "PDFAccessService.getPDF" - {
    "must return notification PDF and metadata when the response is OK" in {

      val application = configureApplication(
        notificationResponse = streamedResponse(OK),
        certificateResponse = HttpResponse(NOT_FOUND)
      )

      running(application) {
        given Materializer = application.materializer

        val service = application.injector.instanceOf[PDFAccessService]
        val result  = service
          .getPDF(subscriptionId, notificationReference, SubmissionDocumentType.Notification)
          .futureValue
          .value

        result.contentLength mustBe Some(pdfContent.length.toLong)
        result.contentType mustBe "application/pdf"

        val bytes = result.content.runFold(ByteString.empty)(_ ++ _).futureValue
        bytes.utf8String mustBe pdfContent
      }
    }

    "must return certificate PDF and metadata when the response is OK" in {
      val application = configureApplication(
        notificationResponse = HttpResponse(NOT_FOUND),
        certificateResponse = streamedResponse(OK)
      )

      running(application) {
        given Materializer = application.materializer

        val service = application.injector.instanceOf[PDFAccessService]
        val result  = service
          .getPDF(subscriptionId, certificateReference, SubmissionDocumentType.Certificate)
          .futureValue
          .value

        result.contentLength mustBe Some(pdfContent.length.toLong)
        result.contentType mustBe "application/pdf"

        val bytes = result.content.runFold(ByteString.empty)(_ ++ _).futureValue
        bytes.utf8String mustBe pdfContent
      }
    }

    "must return None when the PDF is not found" in {
      val application = configureApplication(
        notificationResponse = HttpResponse(NOT_FOUND),
        certificateResponse = HttpResponse(NOT_FOUND)
      )

      running(application) {
        val service = application.injector.instanceOf[PDFAccessService]

        service
          .getPDF(subscriptionId, notificationReference, SubmissionDocumentType.Notification)
          .futureValue mustBe None
      }
    }
  }

  "PDFAccessService.checkPDFStatus" - {
    "must return true when the notification PDF exists" in {
      val application = configureApplication(
        notificationResponse = HttpResponse(OK),
        certificateResponse = HttpResponse(NOT_FOUND),
        notificationStatusResponse = HttpResponse(OK)
      )

      running(application) {
        val service = application.injector.instanceOf[PDFAccessService]

        service
          .checkPDFStatus(subscriptionId, notificationReference, SubmissionDocumentType.Notification)
          .futureValue mustBe true
      }
    }

    "must return false when the certificate PDF is not found" in {
      val application = configureApplication(
        notificationResponse = HttpResponse(NOT_FOUND),
        certificateResponse = HttpResponse(NOT_FOUND),
        certificateStatusResponse = HttpResponse(NOT_FOUND)
      )

      running(application) {
        val service = application.injector.instanceOf[PDFAccessService]

        service
          .checkPDFStatus(subscriptionId, certificateReference, SubmissionDocumentType.Certificate)
          .futureValue mustBe false
      }
    }
  }

  private def configureApplication(
      notificationResponse: HttpResponse,
      certificateResponse: HttpResponse,
      notificationStatusResponse: HttpResponse = HttpResponse(OK),
      certificateStatusResponse: HttpResponse = HttpResponse(OK)
  ): Application = {
    val connector = mock[PDFAccessConnector]

    when(
      connector.getNotificationPDF(
        meq(subscriptionId),
        meq(notificationReference)
      )(using any[HeaderCarrier]())
    ).thenReturn(Future.successful(notificationResponse))

    when(
      connector.getCertificatePDF(
        meq(subscriptionId),
        meq(certificateReference)
      )(using any[HeaderCarrier]())
    ).thenReturn(Future.successful(certificateResponse))

    when(
      connector.headNotificationPDFStatus(
        meq(subscriptionId),
        meq(notificationReference)
      )(using any[HeaderCarrier]())
    ).thenReturn(Future.successful(notificationStatusResponse))

    when(
      connector.headCertificatePDFStatus(
        meq(subscriptionId),
        meq(certificateReference)
      )(using any[HeaderCarrier]())
    ).thenReturn(Future.successful(certificateStatusResponse))

    applicationBuilder()
      .overrides(bind[PDFAccessConnector].toInstance(connector))
      .build()
  }
}

object PDFAccessServiceSpec {
  val subscriptionId        = "0123456789"
  val notificationReference = "NOT0123456789"
  val certificateReference  = "CRT0001234567"
  val pdfContent            = "a-pdf-document"
}

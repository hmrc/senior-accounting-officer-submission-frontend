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

package controllers

import base.SpecBase
import config.ErrorHandler
import controllers.DownloadPdfControllerSpec.*
import models.SubmissionDocumentType
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.http.HeaderNames
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ObjectStoreService
import uk.gov.hmrc.objectstore.client.Md5Hash
import uk.gov.hmrc.objectstore.client.Object as ObjectStoreObject
import uk.gov.hmrc.objectstore.client.ObjectMetadata
import uk.gov.hmrc.objectstore.client.Path

import scala.concurrent.Future

import java.time.Instant

class DownloadPdfControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockObjectStoreService = mock[ObjectStoreService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockObjectStoreService)
  }

  private def application: Application =
    applicationBuilder()
      .overrides(bind[ObjectStoreService].toInstance(mockObjectStoreService))
      .build()

  private def storedPdf(reference: String, documentType: SubmissionDocumentType) =
    ObjectStoreObject(
      Path.Directory(s"/senior-accounting-officer/$reference/").file(documentType.pdfFileName(reference)),
      Source.single(ByteString(pdfContent)),
      ObjectMetadata("application/pdf", pdfContent.length.toLong, Md5Hash("hash"), Instant.now(), Map.empty)
    )

  private def stubDownload(result: Future[Option[ObjectStoreObject[Source[ByteString, NotUsed]]]]): Unit =
    when(mockObjectStoreService.downloadSubmissionPdf(any(), any(), any())(using any())).thenReturn(result)

  "DownloadPdfController" - {
    "notification" - {
      "must stream the pdf as an attachment when it is in object store" in {
        stubDownload(Future.successful(Some(storedPdf(notificationReference, SubmissionDocumentType.Notification))))

        val app = application
        running(app) {
          given Materializer = app.materializer

          val request = FakeRequest(GET, routes.DownloadPdfController.notification(notificationReference).url)
          val result  = route(app, request).value

          status(result) mustEqual OK
          contentType(result).value mustEqual "application/pdf"
          header(HeaderNames.CONTENT_DISPOSITION, result).value mustEqual
            s"attachment; filename=\"${notificationReference}_SAO_Notification.pdf\""
          result.futureValue.body.contentLength.value mustEqual pdfContent.length.toLong
          contentAsString(result) mustEqual pdfContent

          verify(mockObjectStoreService).downloadSubmissionPdf(
            meq(testSaoSubscriptionId),
            meq(notificationReference),
            meq(SubmissionDocumentType.Notification)
          )(using any())
        }
      }

      "must return NOT_FOUND and an error page when the pdf is not in object store" in {
        stubDownload(Future.successful(None))

        val app = application
        running(app) {
          val request = FakeRequest(GET, routes.DownloadPdfController.notification(notificationReference).url)
          val result  = route(app, request).value

          status(result) mustEqual NOT_FOUND
          contentType(result).value mustEqual "text/html"
          contentAsString(result) mustEqual
            app.injector.instanceOf[ErrorHandler].notFoundTemplate(request).futureValue.toString
        }
      }

      "must not swallow an object store failure" in {
        val expectedException = RuntimeException("some exception")
        stubDownload(Future.failed(expectedException))

        val app = application
        running(app) {
          val request = FakeRequest(GET, routes.DownloadPdfController.notification(notificationReference).url)
          val result  = route(app, request).value

          result.failed.futureValue mustBe expectedException
        }
      }
    }

    "certificate" - {
      "must stream the pdf as an attachment when it is in object store" in {
        stubDownload(Future.successful(Some(storedPdf(certificateReference, SubmissionDocumentType.Certificate))))

        val app = application
        running(app) {
          given Materializer = app.materializer

          val request = FakeRequest(GET, routes.DownloadPdfController.certificate(certificateReference).url)
          val result  = route(app, request).value

          status(result) mustEqual OK
          contentType(result).value mustEqual "application/pdf"
          header(HeaderNames.CONTENT_DISPOSITION, result).value mustEqual
            s"attachment; filename=\"${certificateReference}_SAO_Certificate.pdf\""
          result.futureValue.body.contentLength.value mustEqual pdfContent.length.toLong
          contentAsString(result) mustEqual pdfContent

          verify(mockObjectStoreService).downloadSubmissionPdf(
            meq(testSaoSubscriptionId),
            meq(certificateReference),
            meq(SubmissionDocumentType.Certificate)
          )(using any())
        }
      }

      "must return NOT_FOUND and an error page when the pdf is not in object store" in {
        stubDownload(Future.successful(None))

        val app = application
        running(app) {
          val request = FakeRequest(GET, routes.DownloadPdfController.certificate(certificateReference).url)
          val result  = route(app, request).value

          status(result) mustEqual NOT_FOUND
          contentType(result).value mustEqual "text/html"
        }
      }

    }
  }
}

object DownloadPdfControllerSpec {
  val notificationReference = "NOT0123456789"
  val certificateReference  = "CRT0001234567"
  val pdfContent            = "a-pdf-document"
}

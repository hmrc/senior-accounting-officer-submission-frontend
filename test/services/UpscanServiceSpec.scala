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
import connectors.UpscanDownloadConnector
import models.*
import org.mockito.ArgumentMatchers.{eq as meq, *}
import org.mockito.Mockito.*
import org.mockito.verification.VerificationMode
import org.mongodb.scala.bson.ObjectId
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.UpscanSessionRepository
import services.UpscanService.State
import services.UpscanServiceSpec.*
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future
import scala.util.Random

class UpscanServiceSpec extends SpecBase with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val mockUpscanSessionRepository: UpscanSessionRepository = mock[UpscanSessionRepository]
  val mockUpscanDownloadConnector: UpscanDownloadConnector = mock[UpscanDownloadConnector]
  given HeaderCarrier                                      = HeaderCarrier()

  override def beforeEach(): Unit = {
    reset(mockUpscanSessionRepository)
    reset(mockUpscanDownloadConnector)
  }

  def SUT: UpscanService = app.injector.instanceOf[UpscanService]

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(
      bind[UpscanSessionRepository].toInstance(mockUpscanSessionRepository),
      bind[UpscanDownloadConnector].toInstance(mockUpscanDownloadConnector)
    )
    .build()

  "UpscanService.fileUploadState" - {
    "must return State.NoReference when the uploadId does not exist in Mongo" in {
      when(mockUpscanSessionRepository.find(any())).thenReturn(
        Future.successful(None)
      )

      val result = SUT.fileUploadState(testFileReference).futureValue

      result mustBe State.NoReference

      verifyFindByReference(times(1))
      verify(mockUpscanDownloadConnector, times(0)).download(any())(using any())
    }

    "must return State.NoReference when the file upload is in progress" in {
      applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      when(mockUpscanSessionRepository.find(any())).thenReturn(
        Future.successful(
          Some(
            FileUploadState(
              new ObjectId(),
              testFileReference,
              UploadStatus.InProgress
            )
          )
        )
      )

      val result = SUT.fileUploadState(testFileReference).futureValue

      result mustBe State.WaitingForUpscan

      verifyFindByReference(times(1))
      verify(mockUpscanDownloadConnector, times(0)).download(any())(using any())
    }

    "must return State.UploadToUpscanFailed when the file upload has failed" in {
      applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      when(mockUpscanSessionRepository.find(any())).thenReturn(
        Future.successful(
          Some(
            FileUploadState(
              new ObjectId(),
              testFileReference,
              UploadStatus.Failed
            )
          )
        )
      )

      val result = SUT.fileUploadState(testFileReference).futureValue

      result mustBe State.UploadToUpscanFailed

      verifyFindByReference(times(1))
      verify(mockUpscanDownloadConnector, times(0)).download(any())(using any())
    }

    "must return State.Result when the file upload is completed and the file is downloaded from upscan successfully" in {
      applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      when(mockUpscanSessionRepository.find(any())).thenReturn(
        Future.successful(
          Some(
            FileUploadState(
              new ObjectId(),
              testFileReference,
              UploadStatus.UploadedSuccessfully(
                name = "",
                mimeType = "",
                downloadUrl = testDownloadUrl,
                size = None
              )
            )
          )
        )
      )
      val testResponse = HttpResponse(status = OK, body = testFileContent)
      when(mockUpscanDownloadConnector.download(any())(using any())).thenReturn(
        Future.successful(testResponse)
      )

      val result = SUT.fileUploadState(testFileReference).futureValue

      result mustBe State.Result(testFileReference, testFileContent)

      verifyFindByReference(times(1))
      verify(mockUpscanDownloadConnector, times(1)).download(meq(testDownloadUrl))(using any())
    }

    "must return State.DownloadFromUpscanFailed when the file upload is completed but the file download from upscan fails" in {
      applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      when(mockUpscanSessionRepository.find(any())).thenReturn(
        Future.successful(
          Some(
            FileUploadState(
              new ObjectId(),
              testFileReference,
              UploadStatus.UploadedSuccessfully(
                name = "",
                mimeType = "",
                downloadUrl = testDownloadUrl,
                size = None
              )
            )
          )
        )
      )
      val testResponse = HttpResponse(status = BAD_REQUEST, body = testFileContent)
      when(mockUpscanDownloadConnector.download(any())(using any())).thenReturn(
        Future.successful(testResponse)
      )

      val result = SUT.fileUploadState(testFileReference).futureValue

      result mustBe State.DownloadFromUpscanFailed(testResponse)

      verifyFindByReference(times(1))
      verify(mockUpscanDownloadConnector, times(1)).download(meq(testDownloadUrl))(using any())
    }
  }

  def verifyFindByReference(mode: VerificationMode): Unit =
    verify(mockUpscanSessionRepository, mode).find(meq(testFileReference))

}

object UpscanServiceSpec {
  val testDownloadUrl: String   = "/test/url"
  val testFileContent: String   = Random.nextString(10)
  val testFileReference: String = Random.nextString(10)
}

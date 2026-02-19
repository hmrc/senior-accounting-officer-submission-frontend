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

package services

import base.SpecBase
import config.AppConfig
import models.*
import org.mockito.ArgumentMatchers.{eq as meq, *}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.UpscanSessionRepository

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.Future

class MongoBackedUploadProgressTrackerSpec extends SpecBase with GuiceOneAppPerSuite with BeforeAndAfterEach {

  private val instant          = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val mockUpscanSessionRepository: UpscanSessionRepository = mock[UpscanSessionRepository]

  override implicit lazy val app: Application = {
    val mockAppConfig = mock[AppConfig]
    when(mockAppConfig.cacheTtl) thenReturn 1L

    new GuiceApplicationBuilder()
      .overrides(
        bind[UpscanSessionRepository].toInstance(mockUpscanSessionRepository),
        bind[Clock].toInstance(stubClock)
      )
      .build()
  }

  override def beforeEach(): Unit = {
    reset(mockUpscanSessionRepository)
  }

  lazy val progressTracker: UpscanMongoBackedUploadProgressTracker =
    app.injector.instanceOf[UpscanMongoBackedUploadProgressTracker]

  "MongoBackedUploadProgressTracker must" - {

    "implement initialiseUpload that insert a new upload status into Mongo" in {
      val reference = UpscanFileReference("reference")
      val uploadId  = UploadId("upload-id")
      when(mockUpscanSessionRepository.insert(any())).thenReturn(Future.successful((): Unit))

      progressTracker.initialiseUpload(uploadId, reference).futureValue

      verify(mockUpscanSessionRepository, times(1)).insert(argThat { arg =>
        arg.reference == reference &&
        arg.uploadId == uploadId &&
        arg.status == UploadStatus.InProgress
      })
    }

    "implement updateUploadStatus that updates an existing entry in Mongo" in {
      val reference = UpscanFileReference("reference")
      val status    = UploadStatus.UploadedSuccessfully("name", "mimeType", "downloadUrl", size = Some(123))

      when(mockUpscanSessionRepository.updateStatus(any(), any()))
        .thenReturn(Future.successful(status))

      progressTracker.updateUploadStatus(reference, status).futureValue

      verify(mockUpscanSessionRepository, times(1))
        .updateStatus(argThat((_: String) == reference.reference), meq(status))
    }

    "implement getUploadStatus that will return an existing entry in Mongo" in {
      val uploadId                               = UploadId("upload-id")
      val returnedValue: Option[FileUploadState] = None

      when(mockUpscanSessionRepository.findByUploadId(any()))
        .thenReturn(Future.successful(returnedValue))

      val result = progressTracker.getUploadStatus(uploadId).futureValue

      verify(mockUpscanSessionRepository, times(1)).findByUploadId(
        argThat((_: String) == uploadId.value)
      )

      result mustBe returnedValue
    }
  }
}

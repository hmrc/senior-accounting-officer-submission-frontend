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
import models.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{eq as _, *}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import java.time.Instant

class UpscanCallbackDispatcherSpec extends SpecBase with MockitoSugar {

  "UpscanCallbackDispatcher must" - {

    "handle a ReadyCallbackBody" in {
      val mockSessionRepository = mock[SessionRepository]
      val dispatcher            = new UpscanCallbackDispatcher(mockSessionRepository)

      val callback = UpscanSuccessCallback(
        reference = "foo",
        downloadUrl = "http://localhost:8080/download",
        uploadDetails = UpscanFileMetadata(
          uploadTimestamp = Instant.now(),
          checksum = "bar",
          fileMimeType = "application/pdf",
          fileName = "test.pdf",
          size = 123L
        )
      )

      when(mockSessionRepository.updateUploadStatus(any(), any())).thenReturn(Future.successful(true))

      dispatcher.processUpscanCallback(callback).futureValue mustBe true

      val referenceCaptor = ArgumentCaptor.forClass(classOf[AnyRef])
      val statusCaptor    = ArgumentCaptor.forClass(classOf[UploadStatus])

      verify(mockSessionRepository, times(1))
        .updateUploadStatus(referenceCaptor.capture().asInstanceOf[String], statusCaptor.capture())

      referenceCaptor.getValue.toString mustBe "foo"

      val capturedStatus = statusCaptor.getValue.asInstanceOf[UploadStatus.UploadedSuccessfully]

      capturedStatus.name mustBe "test.pdf"
      capturedStatus.mimeType mustBe "application/pdf"
      capturedStatus.downloadUrl mustBe "http://localhost:8080/download"
      capturedStatus.size mustBe Some(123)

    }

    "handle a FailedCallbackBody" in {
      val mockSessionRepository = mock[SessionRepository]
      val dispatcher            = new UpscanCallbackDispatcher(mockSessionRepository)

      val callback = UpscanFailureCallback(
        reference = "foo",
        failureDetails = UpscanFailureDetails(
          failureReason = "QUARANTINE",
          message = "This file has a virus"
        )
      )

      when(mockSessionRepository.updateUploadStatus(any(), any())).thenReturn(Future.successful(true))

      dispatcher.processUpscanCallback(callback).futureValue mustBe true

      val referenceCaptor = ArgumentCaptor.forClass(classOf[AnyRef])
      val statusCaptor    = ArgumentCaptor.forClass(classOf[UploadStatus])

      verify(mockSessionRepository, times(1))
        .updateUploadStatus(referenceCaptor.capture().asInstanceOf[String], statusCaptor.capture())

      referenceCaptor.getValue.toString mustBe "foo"
      statusCaptor.getValue mustBe UploadStatus.Failed

    }
  }
}

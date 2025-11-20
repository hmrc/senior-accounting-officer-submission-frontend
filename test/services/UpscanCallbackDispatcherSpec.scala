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
import connectors.Reference
import controllers.internal.*
import models.UploadStatus
import models.upscan.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{eq as _, *}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Future

import java.net.URL
import java.time.Instant

class UpscanCallbackDispatcherSpec extends SpecBase with MockitoSugar {

  "UpscanCallbackDispatcher" must {

    "handle a ReadyCallbackBody" in {
      val mockUploadProgressTracker = mock[UploadProgressTracker]
      val dispatcher                = new UpscanCallbackDispatcher(mockUploadProgressTracker)

      val callback = UpscanSuccessCallback(
        reference = Reference("foo"),
        downloadUrl = new URL("http://localhost:8080/download"),
        uploadDetails = UpscanUploadDetails(
          uploadTimestamp = Instant.now(),
          checksum = "bar",
          fileMimeType = "application/pdf",
          fileName = "test.pdf",
          size = 123L
        )
      )

      when(mockUploadProgressTracker.registerUploadResult(any(), any())).thenReturn(Future.successful(()))

      dispatcher.handleCallback(callback).futureValue

      val referenceCaptor = ArgumentCaptor.forClass(classOf[AnyRef])
      val statusCaptor    = ArgumentCaptor.forClass(classOf[UploadStatus])

      verify(mockUploadProgressTracker, times(1))
        .registerUploadResult(referenceCaptor.capture().asInstanceOf[Reference], statusCaptor.capture())

      referenceCaptor.getValue.toString mustBe "foo"

      val capturedStatus = statusCaptor.getValue.asInstanceOf[UploadStatus.UploadedSuccessfully]

      capturedStatus.name mustBe "test.pdf"
      capturedStatus.mimeType mustBe "application/pdf"
      capturedStatus.downloadUrl mustBe "/download"
      capturedStatus.size mustBe Some(123)

    }

    "handle a FailedCallbackBody" in {
      val mockUploadProgressTracker = mock[UploadProgressTracker]
      val dispatcher                = new UpscanCallbackDispatcher(mockUploadProgressTracker)

      val callback = UpscanFailureCallback(
        reference = Reference("foo"),
        failureDetails = UpscanFailureDetails(
          failureReason = "QUARANTINE",
          message = "This file has a virus"
        )
      )

      when(mockUploadProgressTracker.registerUploadResult(any(), any())).thenReturn(Future.successful(()))

      dispatcher.handleCallback(callback).futureValue

      val referenceCaptor = ArgumentCaptor.forClass(classOf[AnyRef])
      val statusCaptor    = ArgumentCaptor.forClass(classOf[UploadStatus])

      verify(mockUploadProgressTracker, times(1))
        .registerUploadResult(referenceCaptor.capture().asInstanceOf[Reference], statusCaptor.capture())

      referenceCaptor.getValue.toString mustBe "foo"
      statusCaptor.getValue mustBe UploadStatus.Failed

    }
  }
}

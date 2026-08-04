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

package controllers.testonly

import base.SpecBase
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ObjectStoreService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class DocumentumPackageTestOnlyControllerSpec extends SpecBase with MockitoSugar {

  private val submissionId = "NOT0123456789"
  private val fileName     = "package.zip"
  private val packagePath  = s"/test-only/documentum-package/$submissionId/$fileName"

  "DocumentumPackageTestOnlyController.download" - {
    "return the zip stream when object store contains the package" in {
      val mockObjectStoreService = mock[ObjectStoreService]
      val application            = applicationBuilder()
        .overrides(bind[ObjectStoreService].toInstance(mockObjectStoreService))
        .build()

      running(application) {
        when(
          mockObjectStoreService.downloadDocumentumPackage(any(), any())(using any[HeaderCarrier]())
        ).thenReturn(Future.successful(Some(Source.single(ByteString("zip-content")))))

        val request    = FakeRequest(GET, packagePath)
        val controller = application.injector.instanceOf[DocumentumPackageTestOnlyController]

        val result = controller.download(submissionId, fileName)(request)

        status(result) mustBe OK
        contentType(result) mustBe Some("application/zip")
        verify(mockObjectStoreService, times(1))
          .downloadDocumentumPackage(eqTo(submissionId), eqTo(fileName))(using any[HeaderCarrier]())
      }
    }

    "return not found when object store does not contain the package" in {
      val mockObjectStoreService = mock[ObjectStoreService]
      val application            = applicationBuilder()
        .overrides(bind[ObjectStoreService].toInstance(mockObjectStoreService))
        .build()

      running(application) {
        when(
          mockObjectStoreService.downloadDocumentumPackage(any(), any())(using any[HeaderCarrier]())
        ).thenReturn(Future.successful(None))

        val request    = FakeRequest(GET, packagePath)
        val controller = application.injector.instanceOf[DocumentumPackageTestOnlyController]

        val result = controller.download(submissionId, fileName)(request)

        status(result) mustBe NOT_FOUND
        verify(mockObjectStoreService, times(1))
          .downloadDocumentumPackage(eqTo(submissionId), eqTo(fileName))(using any[HeaderCarrier]())
      }
    }
  }
}

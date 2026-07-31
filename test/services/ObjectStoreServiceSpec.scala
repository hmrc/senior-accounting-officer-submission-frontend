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
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.ObjectStoreServiceSpec.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.Md5Hash
import uk.gov.hmrc.objectstore.client.Object as ObjectStoreObject
import uk.gov.hmrc.objectstore.client.ObjectListing
import uk.gov.hmrc.objectstore.client.ObjectMetadata
import uk.gov.hmrc.objectstore.client.ObjectSummary
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient

import scala.concurrent.Future

import java.time.Instant

class ObjectStoreServiceSpec extends SpecBase with GuiceOneAppPerSuite with BeforeAndAfterEach {

  given HeaderCarrier = HeaderCarrier()

  val mockObjectStoreClient: PlayObjectStoreClient = mock[PlayObjectStoreClient]

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(
      bind[PlayObjectStoreClient].toInstance(mockObjectStoreClient)
    )
    .build()

  def SUT: ObjectStoreService = app.injector.instanceOf[ObjectStoreService]

  override def beforeEach(): Unit = {
    reset(mockObjectStoreClient)
  }

  "isNotificationPdfAvailable" - {
    "when no objects are found in object store must return false" in {
      when(
        mockObjectStoreClient.listObjects(
          path = any(),
          owner = any()
        )(using
          any()
        )
      )
        .thenReturn(Future.successful(ObjectListing(Nil)))

      val result = SUT.isNotificationPdfAvailable(notificationReference)

      result.futureValue mustBe false
    }

    "when objects are found in object store without a pdf must return false" in {
      when(
        mockObjectStoreClient.listObjects(
          path = any(),
          owner = any()
        )(using
          any()
        )
      )
        .thenReturn(
          Future.successful(
            ObjectListing(
              List(
                ObjectSummary(
                  Path
                    .File(
                      Path.Directory(s"senior-accounting-officer/$notificationReference"),
                      s"${notificationReference}_SAO_Notification.txt"
                    ),
                  0,
                  Instant.now()
                )
              )
            )
          )
        )

      val result = SUT.isNotificationPdfAvailable(notificationReference)

      result.futureValue mustBe false
    }

    "when objects are found in object store with a pdf must return true" in {
      when(
        mockObjectStoreClient.listObjects(
          path = any(),
          owner = any()
        )(using
          any()
        )
      )
        .thenReturn(
          Future.successful(
            ObjectListing(
              List(
                ObjectSummary(
                  Path
                    .File(
                      Path.Directory(s"senior-accounting-officer/$notificationReference"),
                      s"${notificationReference}_SAO_Notification.pdf"
                    ),
                  0,
                  Instant.now()
                )
              )
            )
          )
        )

      val result = SUT.isNotificationPdfAvailable(notificationReference)

      result.futureValue mustBe true
    }
  }

  "isCertificatePdfAvailable" - {
    "when no objects are found in object store must return false" in {
      when(
        mockObjectStoreClient.listObjects(
          path = any(),
          owner = any()
        )(using
          any()
        )
      )
        .thenReturn(Future.successful(ObjectListing(Nil)))

      val result = SUT.isCertificatePdfAvailable(certificateReference)

      result.futureValue mustBe false
    }

    "when objects are found in object store without a pdf must return false" in {
      when(
        mockObjectStoreClient.listObjects(
          path = any(),
          owner = any()
        )(using
          any()
        )
      )
        .thenReturn(
          Future.successful(
            ObjectListing(
              List(
                ObjectSummary(
                  Path
                    .File(
                      Path.Directory(s"senior-accounting-officer/$certificateReference"),
                      s"${certificateReference}_SAO_Certificate.txt"
                    ),
                  0,
                  Instant.now()
                )
              )
            )
          )
        )

      val result = SUT.isCertificatePdfAvailable(certificateReference)

      result.futureValue mustBe false
    }

    "when objects are found in object store with a pdf must return true" in {
      when(
        mockObjectStoreClient.listObjects(
          path = any(),
          owner = any()
        )(using
          any()
        )
      )
        .thenReturn(
          Future.successful(
            ObjectListing(
              List(
                ObjectSummary(
                  Path
                    .File(
                      Path.Directory(s"senior-accounting-officer/$certificateReference"),
                      s"${certificateReference}_SAO_Certificate.pdf"
                    ),
                  0,
                  Instant.now()
                )
              )
            )
          )
        )

      val result = SUT.isCertificatePdfAvailable(certificateReference)

      result.futureValue mustBe true
    }
  }

  "downloadDocumentumPackage" - {
    "must download the zip from the SDES object-store path" in {
      val expectedPath = Path
        .Directory(s"/senior-accounting-officer/sdes/$notificationReference/")
        .file(documentumPackageFileName)
      val storedObject = ObjectStoreObject(
        expectedPath,
        Source.single(ByteString("zip-content")),
        ObjectMetadata("application/zip", 11, Md5Hash("hash"), Instant.now(), Map.empty)
      )

      when(
        mockObjectStoreClient.getObject[Source[ByteString, NotUsed]](
          path = any(),
          owner = any()
        )(using
          any(),
          any()
        )
      ).thenReturn(Future.successful(Some(storedObject)))

      val result = SUT.downloadDocumentumPackage(notificationReference, documentumPackageFileName)

      result.futureValue mustBe Some(storedObject.content)
      verify(mockObjectStoreClient).getObject[Source[ByteString, NotUsed]](
        path = meq(expectedPath),
        owner = meq("senior-accounting-officer")
      )(using any(), any())
    }
  }
}

object ObjectStoreServiceSpec {
  val notificationReference     = "NOT0123456789"
  val certificateReference      = "CERT123456789"
  val documentumPackageFileName =
    s"20260731_${notificationReference}_SAO_Notification_OFFICIAL_SENSITIVE.zip"
}

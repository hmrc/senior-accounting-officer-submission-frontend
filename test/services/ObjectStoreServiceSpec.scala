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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar.mock
import org.mockito.ArgumentMatchers.any
import scala.concurrent.Future
import uk.gov.hmrc.objectstore.client.ObjectListing
import play.api.inject.bind
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import services.ObjectStoreServiceSpec.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.ObjectSummary
import uk.gov.hmrc.objectstore.client.Path
import java.time.Instant

class ObjectStoreServiceSpec extends SpecBase with GuiceOneAppPerSuite with BeforeAndAfterEach {

  given HeaderCarrier = HeaderCarrier()

  val mockObjectStoreClient: PlayObjectStoreClient = mock[PlayObjectStoreClient]

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(
      bind[PlayObjectStoreClient].toInstance(mockObjectStoreClient)
    )
    .build()

  def SUT = app.injector.instanceOf[ObjectStoreService]

  override def beforeEach(): Unit = {
    reset(mockObjectStoreClient)
  }

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

object ObjectStoreServiceSpec {
  val notificationReference = "NOT0123456789"
}

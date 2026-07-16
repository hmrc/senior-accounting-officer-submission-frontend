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

package controllers.notification

import base.SpecBase
import controllers.notification.routes as notificationRoutes
import controllers.routes
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.HeaderNames
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.objectstore.client.ObjectListing
import uk.gov.hmrc.objectstore.client.ObjectSummary
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import views.html.notification.NotificationConfirmationView

import scala.concurrent.Future

import java.time.Instant

class NotificationConfirmationControllerSpec extends SpecBase {

  def onwardRoute: Call = Call("GET", "/foo")

  def hardCodedNotRef: String = "SAONOT0123456789"

  "NotificationConfirmation Controller" - {

    "onPageLoad" - {
      "must return OK and a view with no link displayed when object store finds no files" in {

        val mockObjectStoreClient = mock[PlayObjectStoreClient]
        when(
          mockObjectStoreClient.listObjects(
            path = any(),
            owner = any()
          )(using
            any()
          )
        )
          .thenReturn(Future.successful(ObjectListing(Nil)))

        val application = applicationBuilder(userAnswers = Some(completedNotificationReviewAnswers))
          .overrides(bind[PlayObjectStoreClient].toInstance(mockObjectStoreClient))
          .build()

        running(application) {
          val request =
            FakeRequest(GET, notificationRoutes.NotificationConfirmationController.onPageLoad(hardCodedNotRef).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[NotificationConfirmationView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(hardCodedNotRef, false)(using
            request,
            messages(application)
          ).toString
        }
      }

      "must return OK and a view with a link displayed when object store finds some files" in {

        val mockObjectStoreClient = mock[PlayObjectStoreClient]
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
              ObjectListing(List(ObjectSummary(Path.File(Path.Directory("directory"), "fileName"), 0, Instant.now())))
            )
          )

        val application = applicationBuilder(userAnswers = Some(completedNotificationReviewAnswers))
          .overrides(bind[PlayObjectStoreClient].toInstance(mockObjectStoreClient))
          .build()

        running(application) {
          val request =
            FakeRequest(GET, notificationRoutes.NotificationConfirmationController.onPageLoad(hardCodedNotRef).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[NotificationConfirmationView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(hardCodedNotRef, true)(using
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(GET, notificationRoutes.NotificationConfirmationController.onPageLoad(hardCodedNotRef).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "must redirect to the next page for a POST" in {
      val application = applicationBuilder(userAnswers = Some(completedNotificationReviewAnswers))
        .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
        .build()

      running(application) {
        val request = FakeRequest(POST, notificationRoutes.NotificationConfirmationController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        header(HeaderNames.LOCATION, result) mustEqual Some(onwardRoute.url)
      }
    }

  }
}

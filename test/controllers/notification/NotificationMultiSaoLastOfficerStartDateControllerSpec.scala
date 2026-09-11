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

package controllers.notification

import base.SpecBase
import controllers.notification.routes as notificationRoutes
import controllers.routes
import forms.notification.NotificationMultiSaoLastOfficerStartDateFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNotificationNavigator, NotificationNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.notification.{NotificationMultiSaoLastOfficerNamePage, NotificationMultiSaoLastOfficerStartDatePage}
import play.api.Application
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.notification.NotificationMultiSaoLastOfficerStartDateView

import scala.concurrent.Future

import java.time.{LocalDate, ZoneOffset}

class NotificationMultiSaoLastOfficerStartDateControllerSpec extends SpecBase with MockitoSugar {

  val testSaoName: String      = "Firstname Lastname"
  val testValidDate: LocalDate = LocalDate.now(ZoneOffset.UTC).minusDays(1)

  private def form(using app: Application) =
    app.injector
      .instanceOf[NotificationMultiSaoLastOfficerStartDateFormProvider]
      .apply(testSaoName)(using messages(app))

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val notificationMultiSaoLastOfficerStartDateRoute: String =
    notificationRoutes.NotificationMultiSaoLastOfficerStartDateController.onPageLoad(NormalMode).url

  val userAnswers: UserAnswers = emptyUserAnswers
    .set(NotificationMultiSaoLastOfficerNamePage, testSaoName)
    .success
    .value

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, notificationMultiSaoLastOfficerStartDateRoute)

  def postRequest()(using Application): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, notificationMultiSaoLastOfficerStartDateRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> testValidDate.getDayOfMonth.toString,
        "value.month" -> testValidDate.getMonthValue.toString,
        "value.year"  -> testValidDate.getYear.toString
      )

  "test" in {
    given application: Application = applicationBuilder(userAnswers = None).build()

    form.fillAndValidate(testValidDate).errors mustBe Seq.empty
  }

  "NotificationMultiSaoLastOfficerStartDate Controller" - {

    "must return OK and the correct view for a GET" in {

      given application: Application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[NotificationMultiSaoLastOfficerStartDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(testSaoName, form, NormalMode)(using
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must redirect to journey recovery when sao name is not found in the database" in {

      given application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val result = route(application, getRequest()).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswersWithDate =
        userAnswers.set(NotificationMultiSaoLastOfficerStartDatePage, testValidDate).success.value
      given application: Application = applicationBuilder(userAnswers = Some(userAnswersWithDate)).build()

      running(application) {
        val view = application.injector.instanceOf[NotificationMultiSaoLastOfficerStartDateView]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(testSaoName, form.fill(testValidDate), NormalMode)(using
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      given application: Application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[NotificationNavigator].toInstance(new FakeNotificationNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers =
        emptyUserAnswers
          .set(NotificationMultiSaoLastOfficerNamePage, testSaoName)
          .success
          .value
      given application: Application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request =
        FakeRequest(POST, notificationMultiSaoLastOfficerStartDateRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[NotificationMultiSaoLastOfficerStartDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(testSaoName, boundForm, NormalMode)(using
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      given application: Application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      given application: Application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if sao name not found in the database" in {

      given application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

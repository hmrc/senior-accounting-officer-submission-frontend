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
import config.FeatureConfigSupport
import forms.SubmissionTypeFormProvider
import models.FeatureToggle.CombinedJourney
import models.{SubmissionType, UserAnswers}
import navigation.{AgnosticNavigator, FakeAgnosticNavigator}
import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.SubmissionTypePage
import play.api.Configuration
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.SubmissionTypeView

import scala.concurrent.Future

class SubmissionTypeControllerSpec
    extends SpecBase
    with GuiceOneAppPerSuite
    with MockitoSugar
    with BeforeAndAfterEach
    with FeatureConfigSupport {
  def onwardRoute: Call = Call("GET", "/foo")

  given Configuration = app.injector.instanceOf[Configuration]

  lazy val submissionTypeRoute: String = routes.SubmissionTypeController.onPageLoad().url

  val formProvider               = new SubmissionTypeFormProvider()
  val form: Form[SubmissionType] = formProvider()

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    disable(CombinedJourney)
    reset(mockSessionRepository)
    when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
  }

  override def afterEach(): Unit = {
    disable(CombinedJourney)
  }

  "SubmissionType Controller" - {

    "when feature toggle is off" - {

      "must return OK and the correct view for a GET" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(GET, submissionTypeRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SubmissionTypeView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, false)(using request, messages(application)).toString
        }

      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        val userAnswers = emptyUserAnswers.set(SubmissionTypePage, SubmissionType.values.init.head).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .build()

        running(application) {
          val request = FakeRequest(GET, submissionTypeRoute)

          val view = application.injector.instanceOf[SubmissionTypeView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(SubmissionType.values.init.head), false)(using
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {
        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AgnosticNavigator].toInstance(FakeAgnosticNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, submissionTypeRoute)
              .withFormUrlEncodedBody(("value", SubmissionType.values.init.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request =
            FakeRequest(POST, submissionTypeRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[SubmissionTypeView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, false)(using request, messages(application)).toString
        }
      }

      "create a new mongo entry if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[AgnosticNavigator].toInstance(FakeAgnosticNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, submissionTypeRoute)
              .withFormUrlEncodedBody(("value", SubmissionType.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockSessionRepository, times(1)).set(argThat { insertedUserAnswer =>
            insertedUserAnswer.id mustBe userAnswersId
            insertedUserAnswer.data mustBe Json.parse(
              s"""{"submissionType":"${SubmissionType.values.head.toString}"}"""
            )
            true
          })
        }
      }
    }

    "when feature toggle is on" - {

      "must return OK and the correct view for a GET" in {
        enable(CombinedJourney)
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        val view = application.injector.instanceOf[SubmissionTypeView]

        running(application) {
          val request = FakeRequest(GET, submissionTypeRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, true)(using request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        enable(CombinedJourney)
        val userAnswers = emptyUserAnswers.set(SubmissionTypePage, SubmissionType.values.init.head).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .build()

        running(application) {
          val request = FakeRequest(GET, submissionTypeRoute)

          val view = application.injector.instanceOf[SubmissionTypeView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(SubmissionType.values.init.head), true)(using
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {
        enable(CombinedJourney)
        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AgnosticNavigator].toInstance(FakeAgnosticNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, submissionTypeRoute)
              .withFormUrlEncodedBody(("value", SubmissionType.values.init.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        enable(CombinedJourney)
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request =
            FakeRequest(POST, submissionTypeRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[SubmissionTypeView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, true)(using request, messages(application)).toString
        }
      }

      "create a new mongo entry if no existing data is found" in {
        enable(CombinedJourney)
        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[AgnosticNavigator].toInstance(FakeAgnosticNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, submissionTypeRoute)
              .withFormUrlEncodedBody(("value", SubmissionType.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockSessionRepository, times(1)).set(argThat { insertedUserAnswer =>
            insertedUserAnswer.id mustBe userAnswersId
            insertedUserAnswer.data mustBe Json.parse(
              s"""{"submissionType":"${SubmissionType.values.head.toString}"}"""
            )
            true
          })
        }
      }
    }

  }

}

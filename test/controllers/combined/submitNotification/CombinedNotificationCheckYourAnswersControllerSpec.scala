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

package controllers.combined.submitNotification

import base.SpecBase
import config.FeatureToggleSupport
import controllers.combined.submitNotification.CombinedNotificationCheckYourAnswersControllerSpec.exampleNotificationReference
import controllers.combined.submitNotification.routes as submitNotificationRoutes
import controllers.routes
import models.*
import models.FeatureToggle.CombinedJourney
import models.upload.UploadTemplateTableData
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.notification.*
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{NotificationCheckYourAnswersService, NotificationSubmitService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import views.html.combined.submitNotification.CombinedNotificationCheckYourAnswersView

import scala.concurrent.Future

class CombinedNotificationCheckYourAnswersControllerSpec
    extends SpecBase
    with FeatureToggleSupport
    with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(CombinedJourney)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(CombinedJourney)
  }

  "CombinedNotificationCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {
      val mockService = mock[NotificationCheckYourAnswersService]
      when(mockService.getSummaryList(any())(using any())).thenReturn(SummaryList())

      val userAnswers = emptyUserAnswers
        .add(NotificationMoreThanOneSaoPage(NormalMode), false)
        .add(NotificationSingleSaoOfficerNamePage(NormalMode), "Jackson Brown")
        .add(UploadTemplateTablePage, UploadTemplateTableData(rows = Seq.empty, errors = Seq.empty))
        .add(UploadTemplateReviewPage, true)
        .add(NotificationMoreThanOneSaoPage(TransactionMode), false)
        .add(NotificationSingleSaoOfficerNamePage(TransactionMode), "Jackson Brown")

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[NotificationCheckYourAnswersService].toInstance(mockService)
        )
        .build()

      running(application) {
        given request: Request[AnyContentAsEmpty.type] =
          FakeRequest(GET, submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad().url)

        val result     = route(application, request).value
        val view       = application.injector.instanceOf[CombinedNotificationCheckYourAnswersView]
        given Messages = messages(application)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          SummaryList(),
          userAnswers.getFinancialYearEndDate
        ).toString
        verify(mockService).getSummaryList(meq(userAnswers))(using any())
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(GET, submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController
          .onPageLoad(section = Some(JourneySection.Combined))
          .url
      }
    }

    "must redirect to Confirmation page on submit" in {
      val mockNotificationSubmitService = mock[NotificationSubmitService]

      when(mockNotificationSubmitService.submit(any(), any())(using any[HeaderCarrier]()))
        .thenReturn(Future.successful(Right(exampleNotificationReference)))

      val application =
        applicationBuilder(userAnswers = Some(completedNotificationReviewAnswers))
          .overrides(
            bind[NotificationSubmitService].toInstance(mockNotificationSubmitService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, submitNotificationRoutes.CombinedNotificationCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual submitNotificationRoutes.CombinedNotificationConfirmationController
          .onPageLoad(exampleNotificationReference)
          .url()
      }
    }
  }
}

object CombinedNotificationCheckYourAnswersControllerSpec {
  val exampleNotificationReference       = "example notification reference"
  val expectedHttpFailureMessage: String = s"Notification submit HTTP call failed with code ${INTERNAL_SERVER_ERROR}"
}

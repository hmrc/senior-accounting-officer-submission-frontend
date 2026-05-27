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

package controllers.actions

import base.SpecBase
import controllers.routes
import models.UserAnswers
import models.requests.DataRequest
import play.api.http.HeaderNames
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NotificationTaskAvailabilityActionSpec extends SpecBase {

  class UploadHarness extends RequireNotificationUploadUnlockedAction {
    def callFilter(userAnswers: UserAnswers): Future[Option[play.api.mvc.Result]] =
      filter(DataRequest(FakeRequest(), userAnswers.id, userAnswers))
  }

  class SubmitHarness extends RequireSubmitNotificationUnlockedAction {
    def callFilter(userAnswers: UserAnswers): Future[Option[play.api.mvc.Result]] =
      filter(DataRequest(FakeRequest(), userAnswers.id, userAnswers))
  }

  "RequireNotificationUploadUnlockedAction" - {

    "must allow the request when SAO details are complete" in {
      new UploadHarness().callFilter(completedSaoDetailsAnswers).futureValue mustBe None
    }

    "must redirect to the task list when SAO details are incomplete" in {
      val result = new UploadHarness().callFilter(emptyUserAnswers).futureValue.value

      result.header.status mustBe SEE_OTHER
      result.header.headers(HeaderNames.LOCATION) mustBe routes.SubmitNotificationStartController.onPageLoad().url
    }
  }

  "RequireSubmitNotificationUnlockedAction" - {

    "must allow the request when SAO details and upload are complete" in {
      new SubmitHarness().callFilter(completedNotificationUploadAnswers).futureValue mustBe None
    }

    "must redirect to the task list when the upload is incomplete" in {
      val result = new SubmitHarness().callFilter(completedSaoDetailsAnswers).futureValue.value

      result.header.status mustBe SEE_OTHER
      result.header.headers(HeaderNames.LOCATION) mustBe routes.SubmitNotificationStartController.onPageLoad().url
    }
  }
}

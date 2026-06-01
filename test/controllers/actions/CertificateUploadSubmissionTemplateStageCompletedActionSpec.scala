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
import models.CertificateTaskListStage
import models.UserAnswers
import models.requests.DataRequest
import play.api.http.HeaderNames
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CertificateUploadSubmissionTemplateStageCompletedActionSpec extends SpecBase {

  class Harness extends CertificateUploadSubmissionTemplateStageCompletedAction {
    def callFilter(userAnswers: UserAnswers): Future[Option[play.api.mvc.Result]] =
      filter(DataRequest(FakeRequest(), userAnswers.id, userAnswers))
  }

  "CertificateUploadSubmissionTemplateStageCompletedAction" - {

    "must allow the request when submission template has been uploaded are complete" in {
      new Harness().callFilter(userAnswersWithUploadedTemplate).futureValue mustBe None
    }

    "must redirect to the task list when submission template has not yet been uploaded" in {
      val result = new Harness().callFilter(emptyUserAnswers).futureValue.value

      result.header.status mustBe SEE_OTHER
      result.header.headers(HeaderNames.LOCATION) mustBe routes.CertificateTaskListController
        .onPageLoad(CertificateTaskListStage.UploadSubmissionTemplateStage)
        .url
    }
  }
}

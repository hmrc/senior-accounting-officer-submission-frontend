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

package controllers.certificate

import base.SpecBase
import config.AppConfig
import controllers.certificate.routes as certificateRoutes
import models.TaskStatus
import models.certificate.{CertificateTaskListStage, CertificateTaskListState}
import play.api.http.HeaderNames
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.certificate.CertificateTaskListView

class CertificateTaskListControllerSpec extends SpecBase {

  val hubBaseUrl = "http://localhost:10056/senior-accounting-officer"

  "CertificateTaskList Controller" - {
    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            certificateRoutes.CertificateTaskListController
              .onPageLoad(CertificateTaskListStage.ProvideSaoDetailsStage)
              .url
          )

        val result = route(application, request).value

        val view = application.injector.instanceOf[CertificateTaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          CertificateTaskListState(
            provideSaoDetailsStage = TaskStatus.NotStarted,
            uploadSubmissionTemplateStage = TaskStatus.CannotStartYet,
            submitCertificateStage = TaskStatus.CannotStartYet,
            showContinueButton = false
          )
        )(using
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the account homepage on a POST" in {
      AppConfig.setValue("hub-frontend.host", "http://localhost:10056")

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            certificateRoutes.CertificateTaskListController.onSubmit().url
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        header(HeaderNames.LOCATION, result) mustEqual Some(hubBaseUrl)
      }
    }
  }
}

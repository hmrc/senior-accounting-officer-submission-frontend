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
import controllers.certificate.routes as certificateRoutes
import models.certificate.CertificateTaskListStage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.certificate.CertificateUploadFormView

class CertificateUploadFormControllerSpec extends SpecBase {

  "CertificateUploadForm Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCertificateSaoDetails)).build()

      running(application) {
        val request = FakeRequest(GET, certificateRoutes.CertificateUploadFormController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CertificateUploadFormView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(using request, messages(application)).toString
      }
    }

    "must redirect to task list on provide sao details stage when user answers is empty" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, certificateRoutes.CertificateUploadFormController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual certificateRoutes.CertificateTaskListController
          .onPageLoad(CertificateTaskListStage.ProvideSaoDetailsStage)
          .url
      }
    }
  }
}

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
import models.*
import models.certificate.CertificateTaskListStage
import models.upload.{CompanyStatus, CompanyType}
import navigation.{FakeNavigator, Navigator}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.certificate.CertificateReviewUnqualifiedView

class CertificateReviewUnqualifiedControllerSpec extends SpecBase {

  def onwardRoute: Call = Call("GET", "/foo")

  val dummyDate                                     = "2020"
  val unqualifiedDummyData: Seq[UnqualifiedCompany] = Seq(
    UnqualifiedCompany(
      name = "example company name",
      utr = "example company utr",
      crn = "example company crn",
      companyType = CompanyType.LTD,
      companyStatus = CompanyStatus.Administration
    ),
    UnqualifiedCompany(
      name = "example company name 2",
      utr = "example company utr 2",
      crn = "example company crn 2",
      companyType = CompanyType.LTD,
      companyStatus = CompanyStatus.Dormant
    ),
    UnqualifiedCompany(
      name = "example company name 3",
      utr = "example company utr 3",
      crn = "example company crn 3",
      companyType = CompanyType.LTD,
      companyStatus = CompanyStatus.Active
    )
  )

  "CertificateReviewUnqualified Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCertificateSaoDetails)).build()

      running(application) {
        val request = FakeRequest(GET, certificateRoutes.CertificateReviewUnqualifiedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CertificateReviewUnqualifiedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          "Firstname Lastname",
          unqualifiedDummyData,
          unqualifiedDummyData.size,
          dummyDate
        )(using request, messages(application)).toString
      }
    }

    "must redirect to the next page for a POST" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCertificateSaoDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, certificateRoutes.CertificateReviewUnqualifiedController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to task list on provide sao details stage when user answers is empty for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, certificateRoutes.CertificateReviewUnqualifiedController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual certificateRoutes.CertificateTaskListController
          .onPageLoad(CertificateTaskListStage.ProvideSaoDetailsStage)
          .url
      }
    }

    "must redirect to task list on provide sao details stage when user answers is empty for a POST" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(POST, certificateRoutes.CertificateReviewUnqualifiedController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual certificateRoutes.CertificateTaskListController
          .onPageLoad(CertificateTaskListStage.ProvideSaoDetailsStage)
          .url
      }
    }
  }
}

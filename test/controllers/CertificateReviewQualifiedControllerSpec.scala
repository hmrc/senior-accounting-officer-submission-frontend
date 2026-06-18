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
import models.CertificateTaskListStage
import navigation.FakeNavigator
import navigation.Navigator
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.CertificateReviewQualifiedView
import models.QualifiedCompany

class CertificateReviewQualifiedControllerSpec extends SpecBase {

  def onwardRoute: Call = Call("GET", "/foo")

  // TODO: remove when corresponding dummy data removed from controller
  val dummyData = Seq(
    QualifiedCompany(
      name = "example company name",
      utr = "example company utr",
      corporationTax = false,
      valueAddedTax = true,
      paye = false,
      insurancePremiumTax = true,
      stampDutyLandTax = false,
      stampDutyReserveTax = false,
      petroleumRevenueTax = true,
      customsDuties = false,
      exciseDuties = false,
      bankLevy = false,
      additionalInformation = "example additional information"
    ),
    QualifiedCompany(
      name = "example company name 2",
      utr = "example company utr 2",
      corporationTax = false,
      valueAddedTax = true,
      paye = false,
      insurancePremiumTax = true,
      stampDutyLandTax = false,
      stampDutyReserveTax = true,
      petroleumRevenueTax = false,
      customsDuties = false,
      exciseDuties = true,
      bankLevy = false,
      additionalInformation = "example additional information 2"
    )
  )

  "CertificateReviewQualified Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCertificateSaoDetails)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CertificateReviewQualifiedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CertificateReviewQualifiedView]

        status(result) mustEqual OK
        // TODO: test the saoname being passed to the view
        contentAsString(result) mustEqual view("", dummyData, 1)(using request, messages(application)).toString
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
          FakeRequest(POST, routes.CertificateReviewQualifiedController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to task list on provide sao details stage when user answers is empty for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, routes.CertificateReviewQualifiedController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CertificateTaskListController
          .onPageLoad(CertificateTaskListStage.ProvideSaoDetailsStage)
          .url
      }
    }

    "must redirect to task list on provide sao details stage when user answers is empty for a POST" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(POST, routes.CertificateReviewQualifiedController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CertificateTaskListController
          .onPageLoad(CertificateTaskListStage.ProvideSaoDetailsStage)
          .url
      }
    }
  }
}

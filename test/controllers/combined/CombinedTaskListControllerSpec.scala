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

package controllers.combined

import base.SpecBase
import config.{AppConfig, FeatureToggleSupport}
import models.FeatureToggle.CombinedJourney
import org.scalatest.BeforeAndAfterEach
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class CombinedTaskListControllerSpec extends SpecBase with BeforeAndAfterEach with FeatureToggleSupport {

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  "onPageLoad" - {
    "return OK when combined journey is enabled" in {
      enable(CombinedJourney)
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .configure("features.combinedJourneyEnabled" -> true)
          .build()

      running(application) {

        val request = FakeRequest(GET, routes.CombinedTaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustBe OK
      }
    }

    "redirect to hub when combined journey is disabled" in {
      disable(CombinedJourney)
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .configure("features.combinedJourneyEnabled" -> false)
          .build()

      running(application) {

        val request = FakeRequest(GET, routes.CombinedTaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          application.injector.instanceOf[AppConfig].hubBaseUrl
      }
    }
  }

  "onComplete" - {

    "return OK when combined journey is enabled" in {
      enable(CombinedJourney)
      val application =
        applicationBuilder()
          .configure("features.combinedJourneyEnabled" -> true)
          .build()

      running(application) {

        val request = FakeRequest(GET, routes.CombinedTaskListController.onComplete.url)

        val result = route(application, request).value

        status(result) mustBe OK
      }
    }

    "redirect to hub when combined journey is disabled" in {
      disable(CombinedJourney)
      val application =
        applicationBuilder()
          .configure("features.combinedJourneyEnabled" -> false)
          .build()

      running(application) {

        val request = FakeRequest(GET, routes.CombinedTaskListController.onComplete.url)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          application.injector.instanceOf[AppConfig].hubBaseUrl
      }
    }
  }

  "onCompleteSubmit" - {
    "redirect to the hub url when enabled" in {
      enable(CombinedJourney)
      val application =
        applicationBuilder()
          .configure("features.combinedJourneyEnabled" -> true)
          .build()

      running(application) {

        val request = FakeRequest(POST, routes.CombinedTaskListController.onCompleteSubmit.url)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          application.injector.instanceOf[AppConfig].hubBaseUrl
      }
    }

    "redirect to the hub url when disabled" in {
      disable(CombinedJourney)
      val application =
        applicationBuilder()
          .configure("features.combinedJourneyEnabled" -> false)
          .build()

      running(application) {

        val request = FakeRequest(POST, routes.CombinedTaskListController.onCompleteSubmit.url)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          application.injector.instanceOf[AppConfig].hubBaseUrl
      }
    }
  }
}

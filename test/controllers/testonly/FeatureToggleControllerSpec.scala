/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.testonly

import base.SpecBase
import config.{FeatureConfigSupport, FeatureToggleSupport}
import models.FeatureToggle
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.{Application, Configuration}
import views.html.testonly.FeatureToggleView

class FeatureToggleControllerSpec
    extends SpecBase
    with GuiceOneAppPerSuite
    with FeatureConfigSupport
    with FeatureToggleSupport {

  override def fakeApplication(): Application =
    applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .configure(Map("application.router" -> "testOnlyDoNotUseInAppConf.Routes"))
      .build()
  given Messages      = messages(app)
  given Configuration = app.injector.instanceOf[Configuration]

  "FeatureToggleController.get" - {
    "must return 200 with a valid view" in {
      val request      = FakeRequest(GET, controllers.testonly.routes.FeatureToggleController.get().url)
      given Request[?] = request

      val result = route(app, request).value

      val view: FeatureToggleView = app.injector.instanceOf[FeatureToggleView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view().toString
    }
  }

  "FeatureToggleController.update" - {

    "must update all enabled features" in {
      FeatureToggle.values.foreach(feature => disable(feature))

      val featureSubmission: Seq[(String, String)] =
        FeatureToggle.values.toSeq.map(feature => (feature.key, feature.ordinal.toString))

      val request =
        FakeRequest(POST, controllers.testonly.routes.FeatureToggleController.get().url).withFormUrlEncodedBody(
          featureSubmission :+ ("csrfToken" -> "testToken")*
        )

      given Request[?] = request

      val result = route(app, request).value

      val view: FeatureToggleView = app.injector.instanceOf[FeatureToggleView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view().toString

      FeatureToggle.values.foreach(feature => isEnabled(feature) mustBe true)
    }

    "must update all disabled features" in {
      FeatureToggle.values.foreach(feature => enable(feature))

      val request =
        FakeRequest(POST, controllers.testonly.routes.FeatureToggleController.get().url).withFormUrlEncodedBody(
          "csrfToken" -> "testToken"
        )

      given Request[?] = request

      val result = route(app, request).value

      val view: FeatureToggleView = app.injector.instanceOf[FeatureToggleView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view().toString

      FeatureToggle.values.foreach(feature => isEnabled(feature) mustBe false)
    }
  }

}

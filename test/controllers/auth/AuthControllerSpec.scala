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

package controllers.auth

import base.SpecBase
import config.AppConfig
import controllers.actions.{FakeIdentifierAction, IdentifierAction}
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import java.net.URLEncoder

class AuthControllerSpec extends SpecBase {
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[IdentifierAction].to[FakeIdentifierAction])
      .build()

  private val fakeRequest = FakeRequest("GET", "/")

  private val controller = app.injector.instanceOf[AuthController]
  private val appConfig  = app.injector.instanceOf[AppConfig]

  "Auth Controller sign out" must {
    "return 303 to platform sign out and instruct it to go to survey" in {
      val result = controller.signOut()(fakeRequest)

      val encodedContinueUrl  = URLEncoder.encode(appConfig.exitSurveyUrl, "UTF-8")
      val expectedRedirectUrl = s"${appConfig.signOutUrl}?continue=$encodedContinueUrl"

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result).get mustBe expectedRedirectUrl
    }
  }

  "Auth Controller sign out no survey" must {
    "return 303 to platform sign out and instruct it to go to sign out controller's route" in {
      val result = controller.signOutNoSurvey()(fakeRequest)

      val encodedContinueUrl  = URLEncoder.encode(routes.SignedOutController.onPageLoad().url, "UTF-8")
      val expectedRedirectUrl = s"${appConfig.signOutUrl}?continue=$encodedContinueUrl"

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result).get mustBe expectedRedirectUrl
    }
  }
}

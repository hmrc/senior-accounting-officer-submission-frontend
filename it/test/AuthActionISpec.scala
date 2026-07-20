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

import AuthActionISpec.*
import config.AppConfig
import controllers.actions.IdentifierAction
import play.api.http.{HeaderNames, Status}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results
import support.MockAuthHelper.authSession
import support.{ISpecBase, MockAuthHelper, SessionCookieBaker}

class AuthActionISpec extends ISpecBase {

  override def applicationBuilder: GuiceApplicationBuilder =
    GuiceApplicationBuilder()
      .appRoutes { app =>
        val identifierAction = app.injector.instanceOf[IdentifierAction]

        { case ("GET", `testPath`) =>
          identifierAction { request =>
            Results.Ok(testSuccessBody(request.userId))
          }
        }
      }

  def appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  def targetUrl = s"$baseUrl$testPath"

  "An endpoint with Auth Action" when {
    "Auth is missing" must {
      "respond with a 303 to login" in {
        MockAuthHelper.mockAuthOk()

        val response =
          wsClient
            .url(targetUrl)
            .get()
            .futureValue

        MockAuthHelper.verifyAuthWasCalled(times = 0)
        response.status mustBe Status.SEE_OTHER
        response.headers("Location").head must startWith(appConfig.loginContinueUrl)
      }
    }

    "Auth is successful" must {
      "respond with a 200" in {
        MockAuthHelper.mockAuthOk()

        val response =
          wsClient
            .url(targetUrl)
            .withHttpHeaders(
              HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession),
              "Csrf-Token"       -> "nocheck"
            )
            .get()
            .futureValue

        MockAuthHelper.verifyAuthWasCalled()
        response.status mustBe Status.OK
      }
    }

    "Auth did not respond with the required retrievals" must {
      "respond with a 500" in {
        MockAuthHelper.mockAuthNoId()

        val response =
          wsClient
            .url(targetUrl)
            .withHttpHeaders(
              HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession),
              "Csrf-Token"       -> "nocheck"
            )
            .get()
            .futureValue

        MockAuthHelper.verifyAuthWasCalled()
        response.status mustBe Status.INTERNAL_SERVER_ERROR
      }
    }
  }

}

object AuthActionISpec {
  val testPath = "/test-identifier-action"

  def testSuccessBody(userId: String) =
    s"Action Passed Successfully $userId"
}

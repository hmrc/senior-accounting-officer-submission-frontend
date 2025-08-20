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

import config.AppConfig
import org.jsoup.Jsoup
import play.api.http.{HeaderNames, Status}
import support.MockAuthHelper.authSession
import support.{ISpecBase, MockAuthHelper, SessionCookieBaker}

class AuthActionISpec extends ISpecBase {

  val appConfig = app.injector.instanceOf[AppConfig]
  val targetUrl = s"$baseUrl/senior-accounting-officer/submission/notification/guidance"

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
        response.headers("Location").head must startWith(appConfig.loginUrl)
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
        Option(
          Jsoup.parse(response.body).selectFirst("h1").text
        ).get mustBe "Notification template guide"
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

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

package controllers

import base.SpecBase
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.NotificationGuidanceView

class NotificationGuidanceControllerSpec extends SpecBase {

  private val fakeRequest = FakeRequest("GET", "/notification/guidance")

  private val controller = app.injector.instanceOf[NotificationGuidanceController]
  private val view       = app.injector.instanceOf[NotificationGuidanceView]
  "GET /" must {
    "return 200" in {
      val result = controller.onPageLoad()(fakeRequest)

      status(result) mustBe Status.OK
    }

    "return HTML" in {
      val result = controller.onPageLoad()(fakeRequest)

      contentType(result) mustBe Some("text/html")
      charset(result) mustBe Some("utf-8")
    }

    "return correct content html" in {
      given messages: Messages                           = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())
      given request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(GET, routes.NotificationGuidanceController.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustEqual OK
      val content = contentAsString(result)
      content mustEqual view().toString
    }
  }
}

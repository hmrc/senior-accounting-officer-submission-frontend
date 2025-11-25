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
import config.AppConfig
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

// todo: update the guice;
class NotificationTemplateDownloadControllerSpec extends SpecBase with GuiceOneAppPerSuite {

  private val fakeRequest = FakeRequest("GET", "/download/notification/template")

  private val controller = app.injector.instanceOf[DownloadNotificationTemplateController]

  "GET must " - {
    "return a file with correct name,type and headers" in {
      val result = controller.onPageLoad()(fakeRequest)

      status(result) mustBe Status.OK

      val contentDisposition = header("Content-Disposition", result)

      contentDisposition mustBe Some("attachment; filename=test.csv")
      contentType(result) mustBe Some("text/csv")
    }

    "return an internal server error if template file is unavailable" in {
      AppConfig.setValue("templateFile", "nonsense/file/path")
      val result = controller.onPageLoad()(fakeRequest)

      status(result) mustBe Status.INTERNAL_SERVER_ERROR
    }
  }
}

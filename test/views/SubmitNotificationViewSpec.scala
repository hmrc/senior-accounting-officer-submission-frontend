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

package views

import base.ViewSpecBase
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.SubmitNotificationViewSpec.*
import views.html.SubmitNotificationView
import play.api.mvc.Call

class SubmitNotificationViewSpec extends ViewSpecBase[SubmitNotificationView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  def onwardRoute: Call = routes.SubmitNotificationController.onSubmit()

  "SubmitNotificationView" - {
    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = true,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithSubmissionButton(action = onwardRoute, buttonText = "Continue")

    doc.createTestsWithOrWithoutError(hasError = false)
  }
}

object SubmitNotificationViewSpec {
  val pageHeading = "submitNotification"
  val pageTitle   = "submitNotification"
}

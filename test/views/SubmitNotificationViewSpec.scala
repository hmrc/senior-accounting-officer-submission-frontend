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
import play.api.mvc.Call
import views.SubmitNotificationViewSpec.*
import views.html.SubmitNotificationView

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

    doc.createTestsWithCaption(
      pageCaption
    )

    doc.createTestsWithOrWithoutError(hasError = false)

    doc.createTestsWithParagraphs(paragraphs)

    doc.createTestForInsetText(pageInsetText)

    doc.createTestsWithSubmissionButton(action = onwardRoute, buttonText = pageButtonText)
  }
}

object SubmitNotificationViewSpec {
  val pageHeading = "Confirm and submit a notification"
  val pageTitle   =
    "Confirm notification and submit"
  val pageCaption             = "Submit a notification"
  val paragraphs: Seq[String] = Seq(
    "You confirm that this is an official notification to HMRC in relation to the Senior Accounting Officer requirement in accordance with Schedule 46 of the Finance Act 2009.",
    "By sending the notification you agree that the information you have given is complete and correct. If you deliberately give wrong or incomplete information, or do not report changes, the company may have to pay a penalty of £5,000."
  )
  val pageInsetText =
    "If you later realise you’ve submitted incorrect information, contact your Customer Compliance Manager if you have one, or the Customer Engagement Team for support."
  val pageButtonText = "Confirm and submit"
}

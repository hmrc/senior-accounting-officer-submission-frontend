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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.NotificationConfirmationViewSpec.*
import views.html.NotificationConfirmationView

class NotificationConfirmationViewSpec extends ViewSpecBase[NotificationConfirmationView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "NotificationConfirmationView" - {
    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = false,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    "with a confirmation panel that" - {
      "must have the correct title" - {
        doc.getConfirmationPanel.getPanelTitle.createTestWithText(text = panelTitle)
      }

      "must have the correct body" - {
        doc.getConfirmationPanel.getPanelBody.createTestWithText(text = panelBody)
      }
    }

    doc.createTestsWithOrWithoutError(hasError = false)
  }
}

object NotificationConfirmationViewSpec {
  val pageHeading = "Notification submitted"
  val pageTitle   = "Confirmation page"

  val panelTitle = "Notification submitted"
  val panelBody  = "Your reference number SAONOT0123456789"
}

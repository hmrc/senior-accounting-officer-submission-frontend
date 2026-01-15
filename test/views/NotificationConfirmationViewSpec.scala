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
import models.NotificationConfirmationDetails
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.NotificationConfirmationViewSpec.*
import views.html.NotificationConfirmationView

class NotificationConfirmationViewSpec extends ViewSpecBase[NotificationConfirmationView] {

  val notificationConfirmationDetails: NotificationConfirmationDetails =
    NotificationConfirmationDetails(
      companyName = "ABC Limited",
      notificationId = testReferenceNumber,
      notificationDateTime = testDate
    )

  private def generateView(): Document = Jsoup.parse(SUT(notificationConfirmationDetails).toString)

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

      "must have the reference number in bold" in {
        val strongTags = doc.getConfirmationPanel.getPanelBody.select("strong")
        strongTags.size() mustBe 1
        strongTags.get(0).text() mustBe testReferenceNumber
      }
    }

    doc.createTestsWithParagraphs(
      pageParagraphs
    )

    doc.createTestsWithOrWithoutError(hasError = false)
  }
}

object NotificationConfirmationViewSpec {
  val pageHeading    = "Notification submitted"
  val pageTitle      = "Confirmation page"
  val pageParagraphs = Seq(
    "ABC Limited has successfully submitted a notification to let HMRC know who the Senior Accounting Officer is and which company they are responsible for tax accounting arrangements.",
    "Submitted on 17 January 2025 at 14:15am (GMT).",
    "We’ve sent a confirmation email with your reference number to all the contacts you gave during registration.",
    "If you need to keep a record of your answers, you can:",
    "You will be able to see the status of your submission on your account homepage.",
    "You can now submit a certificate."
  )
  val panelTitle          = "Notification submitted"
  val testReferenceNumber = "SAONOT0123456789"
  val panelBody: String   = s"Your reference number $testReferenceNumber"
  val testDate            = "17 January 2025 at 14:15am (GMT)"
}

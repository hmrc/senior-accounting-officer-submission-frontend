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
import org.jsoup.nodes.{Document, Element}
import views.html.NotificationGuidanceView
import views.NotificationGuidanceViewSpec.*

class NotificationGuidanceViewSpec extends ViewSpecBase[NotificationGuidanceView] {

  val doc: Document        = Jsoup.parse(SUT().toString)
  val mainContent: Element = doc.getMainContent

  private def generateView(): Document = {
    val view = SUT()
    Jsoup.parse(view.toString)
  }

  "NotificationGuidanceView" - {

    val doc = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = true,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithOrWithoutError(hasError = false)

    doc.createTestsWithParagraphs(paragraphs)

    doc.getMainContent
      .select("a.govuk-link")
      .get(0)
      .createTestWithLink(downloadLinkText, "#")

    "with the correct SAO Details content" in {
      val headings = mainContent.getElementsByTag("h3")
      headings.get(0).text mustBe "SAO details"
      val listContents = mainContent.getElementsByTag("li")
      listContents.get(0).text mustBe "SAO name: Full name of the SAO."
      listContents.get(1).text mustBe "SAO contact details: Email or phone number of the SAO."
      listContents
        .get(2)
        .text mustBe "SAO start and end date: Dates the SAO held their position during the accounting period."
    }

    "with the correct Accounting Period Details content" in {
      val headings = mainContent.getElementsByTag("h3")
      headings.get(1).text mustBe "Accounting period"
      val listContents = mainContent.getElementsByTag("li")
      listContents.get(3).text mustBe "The start and end date of the accounting period (DD/MM/YYYY)"

    }

    "with the correct Company Details content" in {
      val headings = mainContent.getElementsByTag("h3")
      headings.get(2).text mustBe "Company information"
      val listContents = mainContent.getElementsByTag("li")
      listContents.get(4).text mustBe "Company name: enter the name of the company the SAO was responsible for."
      listContents.get(5).text mustBe "Company UTR: Unique Taxpayer Reference of that company."
      listContents.get(6).text mustBe "Company CRN: Company Registration Number of that company."
      listContents
        .get(7)
        .text mustBe "Company Status: inform if a company is Active, Dormant or Liquidated"
    }

    doc.createTestsWithSubmissionButton(
      action = routes.NotificationGuidanceController.onSubmit(),
      buttonText = "Continue"
    )

  }
}

object NotificationGuidanceViewSpec {
  val pageTitle   = "Notification and certificate submission template guidance"
  val pageHeading = "Submission template guidance"
  val paragraphs  = Seq(
    "This is a step by step guide on how to submit a notification and certificate using the submission template.",
    "Download the submission template.",
    "Fill in all required fields for each company in your group. Each row should represent one company the SAO was responsible for in the previous financial year.",
    "Do not change the layout or structure of the template, if you do, the upload will fail.",
    "Use the guidance in row 3 under each column heading to help you enter the information correctly.",
    "When you’ve completed your template:",
    "Before you submit your certificate, you can check the information you uploaded is correct.",
    "If the information is not correct, upload an updated submission template before continuing.",
    "If there are any errors when you try to upload the template (for example, missing or invalid data), you’ll be shown a list of what to fix.",
    "You can:",
    "Guidance is also included in the template to help you complete each field correctly.",
    "Keep a copy of your uploaded CSV files for your records.",
    "When you upload your completed template:"
  )
  val downloadLinkText = "Download the submission template."
}

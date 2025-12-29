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


    "with the correct content for guidance at the top" in {
      val paras = mainContent.getElementsByTag("p")
      paras.get(0).text mustBe "Use this template to submit your Senior Accounting Officer (SAO) notification. Each row should represent one company the SAO was responsible for in the previous financial year. If there was more than one SAO in the previous year add this on the next row and include the start and end date of the previous SAO."
      paras.get(1).text mustBe "You must fill in all the fields."
    }

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

    "with the correct content for guidance at the bottom" in {
      val paras = mainContent.getElementsByTag("p")
      paras.get(2).text mustBe "If you do not have UTR for some companies, please put in the CRN instead."
    }

    "with the correct link content for notification template download" in {
      val links = mainContent.getElementsByTag("a")
      links.get(0).text mustBe "Download the notification template"
      links.attr("href") mustBe routes.DownloadNotificationTemplateController.onPageLoad().url
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
}

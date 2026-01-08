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
import views.NotificationGuidanceViewSpec.*
import views.html.NotificationGuidanceView

class NotificationGuidanceViewSpec extends ViewSpecBase[NotificationGuidanceView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "NotificationGuidanceView" - {
    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = false,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithOrWithoutError(hasError = false)

    doc.createTestsWithParagraphs(paragraphs)

    doc.createTestsWithBulletPoints(pageBullets)

    doc.createTestsWithSubmissionButton(
      action = controllers.routes.NotificationGuidanceController.onSubmit(),
      buttonText = "Continue"
    )
  }
}

object NotificationGuidanceViewSpec {
  val pageHeading = "Submit a notification"
  val pageTitle   = "Submit a notification"

  val paragraphs: Seq[String] = Seq(
    "Tell HMRC who was responsible for your company’s tax accounting arrangement during the previous financial year.",
    "Each notification must include:",
    "Only one notification can be submitted per financial year. If more than one person acted as SAO, include all of them in the same notification."
  )

  val pageBullets: Seq[String] = Seq(
    "the name and contact details of each person who acted as the Senior Accounting Officer",
    "the accounting period they were responsible during the previous financial year",
    "the name, Unique Taxpayer Reference (UTR) and Company Registration Number (CRN) of every company the SAO was responsible for, if your company is part of a group"
  )
}

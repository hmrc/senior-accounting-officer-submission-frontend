/*
 * Copyright 2026 HM Revenue & Customs
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
import views.TemplateGuidanceViewSpec.*
import views.html.TemplateGuidanceView

class TemplateGuidanceViewSpec extends ViewSpecBase[TemplateGuidanceView] {

  val doc: Document        = Jsoup.parse(SUT().toString)
  val mainContent: Element = doc.getMainContent

  private def generateView(): Document = {
    val view = SUT()
    Jsoup.parse(view.toString)
  }

  "TemplateGuidanceView" - {

    val doc = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = false,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithOrWithoutError(hasError = false)

    doc.createTestsWithParagraphs(paragraphs)

    doc.getMainContent
      .select("a.govuk-link")
      .get(0)
      .createTestWithLink(linkTexts(0), routes.DownloadNotificationTemplateController.downloadFile().url)

    doc.getMainContent
      .select("a.govuk-link")
      .get(1)
      .createTestWithLink(linkTexts(1), routes.DownloadNotificationTemplateController.downloadFile().url)

    doc.createTestsForSubHeadings(pageSubheadings)

    doc.createTestsWithBulletPoints(pageBullets)

    doc.createTestsWithNumberedItems(pageNumberedListItems)

    doc.createTestForInsetText(pageInsetText)

  }

  extension (target: => Document) {
    def createTestsForSubHeadings(subheadings: Seq[String]): Unit = {
      val headings = doc.getMainContent.getElementsByTag("h3")
      "must have expected number of headings" in {
        headings.size() mustBe subheadings.length
      }
      subheadings.zipWithIndex.foreach((subheading, i) => {
        s"must have heading '$subheading'" in {
          headings.get(i).text mustBe subheading
        }
      })
    }
  }
}

object TemplateGuidanceViewSpec {
  val pageTitle               = "Notification and certificate submission template guidance"
  val pageHeading             = "Submission template guidance"
  val paragraphs: Seq[String] = Seq(
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
  val pageSubheadings: Seq[String] = Seq(
    "Step 1: Download and complete a submission template",
    "Step 2: Upload your template",
    "Step 3: Check the information is correct",
    "Step 4: Make your submission",
    "Complete both notification and certificate at the same time",
    "Complete certificate at a different time."
  )
  val pageNumberedListItems: Seq[String] = Seq(
    "Save it as a CSV (comma delimited) file.",
    "Upload it to the service.",
    "Check that all details are correct.",
    "Submit the file to complete your notification and certificate.",
    "Start your submission by uploading the template and submitting your notification.",
    "After notification is submitted, complete the certificate journey to finish your submission.",
    "Upload your template again with certificate information filled out.",
    "Complete the certificate journey and sign the declaration."
  )
  val pageBullets: Seq[String] = Seq(
    "correct the errors in your Excel file",
    "save again as a CSV",
    "upload the file again",
    "notification details will be used to complete your notification",
    "certificate details will be used to complete your certificate"
  )
  val pageInsetText =
    "If you only completed the notification section, you’ll need to re-upload the same template later when you’re ready to complete your certificate."

  val linkTexts: Seq[String] = Seq("Download the submission template.", "upload an updated submission template")

}

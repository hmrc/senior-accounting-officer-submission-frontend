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
import config.AppConfig
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
    AppConfig.setValue("hub-frontend.host", hubUrl)
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

    doc.createTestsWithBulletPoints(
      pageListItems
    )

    doc.getMainContent
      .select("li span")
      .get(0).createTestWithText(pageListItems(0))
//      .createTestWithLink(
//        linkText = pageListItems(0),
//        destinationUrl = "#"
//      )

    doc.getMainContent
      .select("li span")
      .get(1).createTestWithText(pageListItems(1))
//      .createTestWithLink(
//        linkText = pageListItems(1),
//        destinationUrl = "#"
//      )

    doc.getMainContent
      .select("li a")
      .get(0)
      .createTestWithLink(
        linkText = pageDownload,
        destinationUrl = "#"
      )

    doc.getMainContent
      .select("li a")
      .get(1)
      .createTestWithLink(
        linkText = pagePrint,
        destinationUrl = "#"
      )

    doc.getMainContent
      .selectFirst("p a")
      .createTestWithLink(
        linkText = "account homepage.",
        destinationUrl = s"$hubUrl/senior-accounting-officer"
      )

    doc.createTestsForSubHeadings(
      pageSubheadings
    )

    doc.createTestsWithOrWithoutError(hasError = false)
  }

  extension (target: => Document) {
    def createTestsForSubHeadings(subheadings: Seq[String]): Unit = {
      val headings = target.getMainContent.getElementsByTag("h2")
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

object NotificationConfirmationViewSpec {
  val pageHeading                 = "Notification submitted"
  val pageTitle                   = "Confirmation page"
  val pageParagraphs: Seq[String] = Seq(
    "We’ve sent a confirmation email to all the contacts you gave during registration.",
    "To keep a record of your submission, you can:",
    "Your notification has been received by HMRC. A member of compliance staff may contact you if they need more information.",
    "You can now submit a Senior Accounting Officer certificate or another notification on behalf of another SAO in your group on your account homepage.",
  )
  val panelTitle                 = "Notification submitted"
  val testReferenceNumber        = "SAONOT0123456789"
  val panelBody: String          = s"Your reference number $testReferenceNumber"
  val testDate                   = "17 January 2025 at 14:15am (GMT)"
  val pageListItems: Seq[String] =
    Seq(
      "Download a PDF - save a copy of all the answers you submitted now. You may not be able to download a PDF if you leave this page",
      "Print this page - print a paper copy of this confirmation page"
    )
  val pageDownload = "Download a PDF"
  val pagePrint = "Print this page"
  val pageSubheadings: Seq[String] = Seq("What happens next")
  val hubUrl                       = "testHubUrl"
}

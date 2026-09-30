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

package views.certificate

import base.ViewSpecBase
import config.AppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.certificate.CertificateConfirmationView

import CertificateConfirmationViewSpec.*

class CertificateConfirmationViewSpec extends ViewSpecBase[CertificateConfirmationView] {

  private def generateView(displayLink: Boolean): Document = Jsoup.parse(SUT(certificateRef, displayLink).toString)
  private val appConfig                                    = app.injector.instanceOf[AppConfig]
  "CertificateConfirmationView" - {

    "when displayLink is true" - {
      val displayLink   = true
      val doc: Document = generateView(displayLink)

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
          strongTags.get(0).text() mustBe certificateRef
        }
      }

      doc.createTestsWithParagraphs(
        pageParagraphs
      )

      doc.createTestsWithBulletPoints(
        pageListItemsWhenLinkDisplayed
      )

      doc.getMainContent
        .select("li span a")
        .get(0)
        .createTestWithLink(
          linkText = pageDownload,
          destinationUrl = pageDownloadUrl
        )

      doc.getMainContent
        .select("li span a")
        .get(1)
        .createTestWithLink(
          linkText = pagePrint,
          destinationUrl = "#"
        )

      doc.createTestForAccountHomepageLink(expectedLinkText = pageAccountPage, destinationUrl = appConfig.hubBaseUrl)
      doc.createTestForInsetText(pageInsets)
      doc.createTestsForSubheadings(pageSubheadings)
      doc.createTestsWithOrWithoutError(hasError = false)
    }

    "when displayLink is false" - {
      val displayLink   = false
      val doc: Document = generateView(displayLink)

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
          strongTags.get(0).text() mustBe certificateRef
        }
      }

      doc.createTestsWithParagraphs(
        pageParagraphs
      )

      doc.createTestsWithBulletPoints(
        pageListItemsWhenLinkNotDisplayed
      )

      doc.getMainContent
        .select("li span a")
        .get(0)
        .createTestWithLink(
          linkText = pagePrint,
          destinationUrl = "#"
        )

      doc.createTestForAccountHomepageLink(expectedLinkText = pageAccountPage, destinationUrl = appConfig.hubBaseUrl)
      doc.createTestsForSubheadings(pageSubheadings)
      doc.createTestsWithOrWithoutError(hasError = false)
    }
  }

  extension (target: => Document) {
    def createTestsForSubheadings(subheadings: Seq[String]): Unit = {
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

    def createTestForAccountHomepageLink(expectedLinkText: String, destinationUrl: String): Unit = {
      val homepageLink = target.select(s"a[href=${appConfig.hubBaseUrl}]")
      println(homepageLink)

      "must have account homepage link" in {
        homepageLink.size() mustBe 2
        homepageLink.get(1).text() mustBe expectedLinkText
        homepageLink.get(1).attr("href") mustBe destinationUrl
      }
    }
  }

}

object CertificateConfirmationViewSpec {
  val pageHeading       = "Certificate submitted"
  val pageTitle: String = s"$pageHeading"

  val certificateRef          = "SAOCRT0123456789"
  val pageDownloadUrl: String =
    s"/senior-accounting-officer/submission/certificate/download?certificateReference=$certificateRef"
  val panelTitle        = "Certificate submitted"
  val panelBody: String = s"Your reference number $certificateRef"

  val pageParagraphs: Seq[String] = Seq(
    "HMRC has received your certificate. We’ve sent a confirmation email to all the contacts you provided during registration.",
    "To keep a record of your submission, you can:",
    "Someone from HMRC may contact you if they need more information.",
    "You can submit a notification or another certificate from your account homepage."
  )
  val pageListItemsWhenLinkDisplayed: Seq[String] = Seq(
    "download a PDF to save a copy of all the answers. You may not be able to do this after you leave this page",
    "print this page to keep a paper copy of your confirmation"
  )

  val pageInsets =
    "If you later realise the information is incorrect, contact your Customer Compliance Manager (CCM) if you have one, or email wmbc.saomailbox@hmrc.gov.uk for support."

  val pageListItemsWhenLinkNotDisplayed: Seq[String] = Seq(
    "print this page to keep a paper copy of your confirmation"
  )
  val pageDownload                 = "download a PDF"
  val pagePrint                    = "print this page"
  val pageAccountPage              = "account homepage"
  val pageSubheadings: Seq[String] = Seq("What happens next")
}

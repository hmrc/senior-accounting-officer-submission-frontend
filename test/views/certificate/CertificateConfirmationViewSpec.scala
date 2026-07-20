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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.certificate.CertificateConfirmationView

import CertificateConfirmationViewSpec.*

class CertificateConfirmationViewSpec extends ViewSpecBase[CertificateConfirmationView] {

  private def generateView(displayLink: Boolean): Document = Jsoup.parse(SUT(certificateRef, displayLink).toString)

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
          destinationUrl = "#"
        )

      doc.getMainContent
        .select("li span a")
        .get(1)
        .createTestWithLink(
          linkText = pagePrint,
          destinationUrl = "#"
        )

      doc.createTestsForSubheadings(pageSubheadings)
      doc.createTestsWithOrWithoutError(hasError = false)
      doc.createTestsWithSubmissionButton(
        controllers.certificate.routes.CertificateConfirmationController.onSubmit(),
        "Continue"
      )
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

      doc.createTestsForSubheadings(pageSubheadings)
      doc.createTestsWithOrWithoutError(hasError = false)
      doc.createTestsWithSubmissionButton(
        controllers.certificate.routes.CertificateConfirmationController.onSubmit(),
        "Continue"
      )
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
  }

}

object CertificateConfirmationViewSpec {
  val pageHeading = "Certificate submitted"
  val pageTitle   = "Certificate submitted"

  val certificateRef    = "SAOCRT0123456789"
  val panelTitle        = "Certificate submitted"
  val panelBody: String = s"Your reference number $certificateRef"

  val pageParagraphs: Seq[String] = Seq(
    "We’ve sent a confirmation email to all the contacts you gave during registration.",
    "If you need to keep a record of your answers, you can:",
    "Your certificate has been received by HMRC. A member of compliance staff may contact you if they need more information.",
    "You can now make another submission on your account homepage."
  )
  val pageListItemsWhenLinkDisplayed: Seq[String] = Seq(
    "Download a PDF - save a copy of all the answers you submitted now. You may not be able to download a PDF if you leave this page",
    "Print this page - print a paper copy of this confirmation page"
  )
  val pageListItemsWhenLinkNotDisplayed: Seq[String] = Seq(
    "Print this page - print a paper copy of this confirmation page"
  )
  val pageDownload                 = "Download a PDF"
  val pagePrint                    = "Print this page"
  val pageSubheadings: Seq[String] = Seq("What happens next")
}

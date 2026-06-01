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
import forms.CertificateDeclarationStandInFormProvider
import models.CertificateDeclarationStandIn
import models.Mode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.CertificateDeclarationStandInViewSpec.*
import views.html.CertificateDeclarationStandInView

class CertificateDeclarationStandInViewSpec extends ViewSpecBase[CertificateDeclarationStandInView] {

  private val formProvider = app.injector.instanceOf[CertificateDeclarationStandInFormProvider]
  private val form: Form[CertificateDeclarationStandIn] = formProvider()

  private def generateView(form: Form[CertificateDeclarationStandIn], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "CertificateDeclarationStandInView" - {

    Mode.values.foreach { mode =>
      s"when using $mode" - {
        "when the form is not filled in" - {
          val doc = generateView(form, mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          doc.createTestsWithParagraphs(pageParagraphs)
          doc.createTestsWithBulletPoints(pageBullets)
          doc.createTestForInsetText(pageInset)
          doc.createTestsForSubHeadings(pageSubheading)

          doc.createTestMustShowNumberOfInputs(2)
          doc.createTestMustShowTextInput(
            name = "StandInName",
            label = pageInput1Label,
            value = "",
            hint = Some(pageInput1Hint),
            hasError = false
          )
          doc.createTestMustShowTextInput(
            name = "SaoName",
            label = pageInput2Label,
            value = "",
            hint = Some(pageInput2Hint),
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateDeclarationStandInController.onSubmit(mode),
            buttonText = pageButtonText
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("StandInName" -> testInput1Value, "SaoName" -> testInput2Value)), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          doc.createTestsWithParagraphs(pageParagraphs)
          doc.createTestsWithBulletPoints(pageBullets)
          doc.createTestForInsetText(pageInset)
          doc.createTestsForSubHeadings(pageSubheading)

          doc.createTestMustShowNumberOfInputs(2)
          doc.createTestMustShowTextInput(
            name = "StandInName",
            label = pageInput1Label,
            value = testInput1Value,
            hint = Some(pageInput1Hint),
            hasError = false
          )
          doc.createTestMustShowTextInput(
            name = "SaoName",
            label = pageInput2Label,
            value = testInput2Value,
            hint = Some(pageInput2Hint),
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateDeclarationStandInController.onSubmit(mode),
            buttonText = pageButtonText
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form has errors" - {
          val doc = generateView(form.withError("StandInName", "broken").withError("SaoName", "broken"), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = true
          )

          doc.createTestMustShowNumberOfInputs(2)
          doc.createTestMustShowTextInput(
            name = "StandInName",
            label = pageInput1Label,
            value = "",
            hint = Some(pageInput1Hint),
            hasError = true
          )
          doc.createTestMustShowTextInput(
            name = "SaoName",
            label = pageInput2Label,
            value = "",
            hint = Some(pageInput2Hint),
            hasError = true
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateDeclarationStandInController.onSubmit(mode),
            buttonText = pageButtonText
          )

          doc.createTestsWithOrWithoutError(
            hasError = true
          )
        }
      }
    }
  }

  extension (doc: => Document) {
    def createTestsForSubHeadings(subheadings: String): Unit = {
      val subheadings = doc.getMainContent.getElementsByTag("h2")
      "must have expected number of subheadings" in {
        subheadings.size() mustBe 1
      }
      s"must have heading '$pageSubheading'" in {
        subheadings.get(0).text mustBe pageSubheading
      }
    }
  }
}

object CertificateDeclarationStandInViewSpec {
  val pageTitle   = "Confirm the certificate – Senior Accounting Officer notification and certificate"
  val pageCaption = "Submit a certificate"
  val pageHeading = "Confirm the certificate"

  val pageParagraphs: Seq[String] = Seq(
    "It is your responsibility to make sure the SAO has reviewed and approved everything before you submit.",
    "By submitting this certificate, you confirm that:",
    "If you deliberately give wrong or incomplete information, or do not report changes, the SAO may have to pay a penalty of £5,000."
  )

  val pageBullets: Seq[String] = Seq(
    "the information is complete and correct",
    "you have been authorised by the SAO to submit this certificate"
  )

  val pageInset =
    "If you realise the information you submitted is incorrect, contact HMRC using your usual compliance contact or existing support channels."
  val pageSubheading  = "Declaration"
  val pageInput1Label = "I confirm that I am authorised to submit this certificate"
  val pageInput1Hint  = "Insert your full name here (person submitting)"
  val pageInput2Label = "Name of the SAO who authorised you"
  val pageInput2Hint  = "Insert full name"
  val testInput1Value = "test value 1"
  val testInput2Value = "test value 2"
  val pageButtonText  = "Confirm"

}

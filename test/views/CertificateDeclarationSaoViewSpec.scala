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
import forms.CertificateDeclarationSaoFormProvider
import models.Mode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.CertificateDeclarationSaoViewSpec.*
import views.html.CertificateDeclarationSaoView

class CertificateDeclarationSaoViewSpec extends ViewSpecBase[CertificateDeclarationSaoView] {

  private val formProvider       = app.injector.instanceOf[CertificateDeclarationSaoFormProvider]
  private val form: Form[String] = formProvider()

  private def generateView(form: Form[String], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "CertificateDeclarationSaoView" - {

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

          doc.createTestsWithASingleTextInput(
            name = "value",
            label = pageLabel,
            value = "",
            hint = pageHint,
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateDeclarationSaoController.onSubmit(mode),
            buttonText = pageButton
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )

          doc.createTestsWithLargeCaption(pageCaption)
          doc.createTestsWithParagraphs(pageParagraphs)
          doc.createTestsWithBulletPoints(pageBullets)
          doc.createTestForInsetText(pageInsetText)
          doc.createTestsForSubHeadings(pageSubheadings)
          doc.createTestsForInputWidth()
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("value" -> testInputValue)), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          doc.createTestsWithASingleTextInput(
            name = "value",
            label = pageLabel,
            value = testInputValue,
            hint = pageHint,
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateDeclarationSaoController.onSubmit(mode),
            buttonText = pageButton
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )

          doc.createTestsWithLargeCaption(pageCaption)
          doc.createTestsWithParagraphs(pageParagraphs)
          doc.createTestsWithBulletPoints(pageBullets)
          doc.createTestForInsetText(pageInsetText)
          doc.createTestsForSubHeadings(pageSubheadings)
          doc.createTestsForInputWidth()
        }

        "when the form has errors" - {
          val doc = generateView(form.withError("value", "broken"), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = true
          )

          doc.createTestsWithASingleTextInput(
            name = "value",
            label = pageLabel,
            value = "",
            hint = pageHint,
            hasError = true
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.CertificateDeclarationSaoController.onSubmit(mode),
            buttonText = pageButton
          )

          doc.createTestsWithOrWithoutError(
            hasError = true
          )

          doc.createTestsWithLargeCaption(pageCaption)
          doc.createTestsWithParagraphs(pageParagraphs)
          doc.createTestsWithBulletPoints(pageBullets)
          doc.createTestForInsetText(pageInsetText)
          doc.createTestsForSubHeadings(pageSubheadingsWithError)
          doc.createTestsForInputWidth()
        }
      }
    }
  }
}

object CertificateDeclarationSaoViewSpec {
  val pageHeading                                 = "Confirm the certificate"
  val pageTitle                                   = "Confirm the certificate"
  val pageLabel                                   = "I am the Senior Accounting Officer with the authority to submit this certificate:"
  val pageHint: Some[String]                      = Some("Insert full name")
  val pageSubheadings: Seq[String]                = Seq("Declaration")
  val pageSubheadingsWithError: Seq[String]       = Seq("There is a problem", "Declaration")
  val pageCaption                                 = "Submit a certificate"
  val pageButton                                  = "Confirm"
  val testInputValue                              = "myTestInputValue"
  val pageParagraphs: Seq[String] = Seq(
    "As the SAO it is your responsibility to make sure you have reviewed and approved everything before you submit.",
    "By submitting this certificate, you confirm that:",
    "if you deliberately give wrong or incomplete information, or do not report changes, the SAO may have to pay a penalty of £5,000."
  )
  val pageBullets: Seq[String] = Seq(
    "the information is complete and correct",
    "you are the SAO submitting this certificate"
  )
  val pageInsetText =
    "If you realise the information you submitted is incorrect, contact HMRC using your usual compliance contact or existing support channels."
}

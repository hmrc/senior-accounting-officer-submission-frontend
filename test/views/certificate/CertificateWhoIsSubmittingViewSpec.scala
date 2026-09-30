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
import forms.certificate.CertificateWhoIsSubmittingFormProvider
import models.*
import models.certificate.CertificateWhoIsSubmitting
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.certificate.CertificateWhoIsSubmittingView

import CertificateWhoIsSubmittingViewSpec.*

class CertificateWhoIsSubmittingViewSpec extends ViewSpecBase[CertificateWhoIsSubmittingView] {

  private val formProvider                           = app.injector.instanceOf[CertificateWhoIsSubmittingFormProvider]
  private val form: Form[CertificateWhoIsSubmitting] = formProvider()

  private def generateView(form: Form[CertificateWhoIsSubmitting], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "CertificateWhoIsSubmittingView" - {

    Seq(NormalMode, TransactionMode).foreach { mode =>
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

          doc.createTestsWithLargeCaption(pageCaption)
          doc.createTestsWithParagraphs(paragraphs)
          doc.createTestsForSubHeadings(subheadings)

          doc.createTestsWithRadioButtons(
            name = "value",
            radios = List(
              radio(value = option1key, label = option1Label),
              radio(value = option2key, label = option2Label)
            ),
            isChecked = None,
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.certificate.routes.CertificateWhoIsSubmittingController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("value" -> option1key)), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          doc.createTestsWithLargeCaption(pageCaption)
          doc.createTestsWithParagraphs(paragraphs)
          doc.createTestsForSubHeadings(subheadings)

          doc.createTestsWithRadioButtons(
            name = "value",
            radios = List(
              radio(value = option1key, label = option1Label),
              radio(value = option2key, label = option2Label)
            ),
            isChecked = Some(radio(value = option1key, label = option1Label)),
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.certificate.routes.CertificateWhoIsSubmittingController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
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

          doc.createTestsWithLargeCaption(pageCaption)
          doc.createTestsWithParagraphs(paragraphs)
          doc.createTestsForSubHeadings(subheadingsPageError)

          doc.createTestsWithRadioButtons(
            name = "value",
            radios = List(
              radio(value = option1key, label = option1Label),
              radio(value = option2key, label = option2Label)
            ),
            isChecked = None,
            hasError = true
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.certificate.routes.CertificateWhoIsSubmittingController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = true
          )
        }
      }
    }

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

object CertificateWhoIsSubmittingViewSpec {
  val pageCaption             = "Submit a certificate"
  val pageHeading             = "Certificate submission and authorisation"
  val pageTitle: String       = s"$pageHeading - $pageCaption"
  val paragraphs: Seq[String] = Seq(
    "HMRC needs to know if the certificate will be submitted by the SAO or by someone authorised to act on their behalf.",
    "The SAO must review and approve the certificate and authorise anyone submitting on their behalf."
  )
  val subheadings: Seq[String] = Seq(
    "Who is submitting the certificate?"
  )
  val subheadingsPageError: Seq[String] = Seq("There is a problem") ++ subheadings
  val option1key                        = "sao"
  val option1Label                      = "The SAO"
  val option2key                        = "standIn"
  val option2Label                      = "A person authorised to submit on behalf of the SAO"
}

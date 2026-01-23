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
import org.jsoup.nodes.Document
import views.SubmitCertificateStartViewSpec.*
import views.html.SubmitCertificateStartView

class SubmitCertificateStartViewSpec extends ViewSpecBase[SubmitCertificateStartView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "SubmitCertificateStartView" - {
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

    doc.createTestsWithCaption(pageCaption)

    doc.createTestsWithBulletPoints(pageBullets)

    doc.getMainContent
      .selectFirst("p a")
      .createTestWithLink(
        linkText = uploadSubmissionTemplateLinkText,
        destinationUrl = "#"
      )

    doc.createTestsWithSubmissionButton(
      action = routes.SubmitCertificateStartController.onSubmit(),
      buttonText = "Continue"
    )
  }
}

object SubmitCertificateStartViewSpec {
  val pageHeading = "Submit a certificate"
  val pageTitle   = "Submit a certificate"
  val pageCaption = "Submit a certificate"
  val paragraphs: Seq[String] = Seq(
    "The company details and tax regimes you provided in the uploaded template will be used to prepare your certificate.",
    "If you need to update company details or tax information, you can upload another submission template again before continuing.",
    "If you have qualified companies:"
  )
  val pageBullets: Seq[String] = Seq(
    "You’ll need to review the list of companies identified as qualified and confirm that is it correct before continuing.",
    "You’ll also need to confirm the number of qualified company in the certificate statement before submitting the certificate."
  )
  val uploadSubmissionTemplateLinkText = "upload another submission template"
}

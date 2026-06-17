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
import org.jsoup.nodes.Document
import views.CertificateReviewQualifiedViewSpec.*
import views.html.CertificateReviewQualifiedView
import models.QualifiedCompany

class CertificateReviewQualifiedViewSpec extends ViewSpecBase[CertificateReviewQualifiedView] {

  private def generateView(qualifiedCompanies: Seq[QualifiedCompany]): Document =
    Jsoup.parse(SUT(qualifiedCompanies, 1, 2).toString)

  "CertificateReviewQualifiedView" - {
    val doc: Document = generateView(Seq())

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = true,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithOrWithoutError(hasError = false)

    doc.createTestsWithParagraphs(paragraphs)

    doc.createTestsWithSubmissionButton(
      action = routes.CertificateReviewQualifiedController.onSubmit(),
      buttonText = "Continue"
    )
  }
}

object CertificateReviewQualifiedViewSpec {
  val pageHeading = "Review the companies with a qualified certificate"
  val pageTitle   = "Review the companies with a qualified certificate"
  val paragraphs  = Seq(
    "This list is taken from the certificate details in the submission template you uploaded. There were 1 companies the SAO was responsible for during the financial year.",
    "If the information listed is not correct, upload an updated submission template before continuing.",
    "In accordance with paragraph 2 of Schedule 46 to the Finance Act 2009, I Jackson Brown, the Senior Accounting Officer, hereby certify that throughout the company’s financial year ended 31 December 2024, 1 companies did not have appropriate tax accounting arrangements."
  )
}

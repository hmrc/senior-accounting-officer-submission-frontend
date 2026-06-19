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
import models.UnqualifiedCompany
import models.upload.{CompanyStatus, CompanyType}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.CertificateReviewUnqualifiedViewSpec.*
import views.html.CertificateReviewUnqualifiedView

class CertificateReviewUnqualifiedViewSpec extends ViewSpecBase[CertificateReviewUnqualifiedView] {
  val unqualifiedCompanyExample: UnqualifiedCompany =
    UnqualifiedCompany(
      name = "Steve",
      utr = "example utr",
      crn = Some("example crn"),
      companyType = CompanyType.LTD,
      companyStatus = CompanyStatus.Active
    )
  private def generateView(
      saoName: String,
      unqualifiedCompanies: Seq[UnqualifiedCompany],
      companyCount: Int,
      dummyDate: String
  ): Document = Jsoup.parse(SUT(saoName, unqualifiedCompanies, companyCount, dummyDate).toString)

  "CertificateReviewUnqualifiedView" - {
    "will populate the table correctly when data is present" - {

      val doc: Document = generateView(unqualifiedCompanyExample.name, Seq(unqualifiedCompanyExample), 1, "1996")

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithLargeCaption(pageCaption)

      doc.createTestsWithOrWithoutError(hasError = false)

      doc.createTestsWithParagraphs(paragraphs)

      doc.createTestsWithSubmissionButton(
        action = routes.CertificateReviewUnqualifiedController.onSubmit(),
        buttonText = "Continue"
      )

      doc
        .select(linkLocator)
        .get(0)
        .createTestWithLink(linkText, routes.CertificateUploadFormController.onPageLoad().url)

      // TODO: Test Table
    }

    "will populate the table correctly when no data is present" - {

      val doc: Document = generateView("", Seq(), 0, "")

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithLargeCaption(pageCaption)

      doc.createTestsWithOrWithoutError(hasError = false)

      doc.createTestsWithParagraphs(paragraphsWithNoData)

      doc.createTestsWithSubmissionButton(
        action = routes.CertificateReviewUnqualifiedController.onSubmit(),
        buttonText = "Continue"
      )

      doc
        .select(linkLocator)
        .get(0)
        .createTestWithLink(linkText, routes.CertificateUploadFormController.onPageLoad().url)

      // TODO: Test Table
    }
  }
}

object CertificateReviewUnqualifiedViewSpec {
  val pageHeading             = "Review the companies with an unqualified certificate"
  val pageTitle               = "Review the companies with an unqualified certificate"
  val pageCaption             = "Submit a certificate"
  val linkLocator             = ".govuk-body:nth-of-type(2) .govuk-link"
  val linkText                = "upload an updated submission template"
  val paragraphs: Seq[String] = Seq(
    "This list is taken from the certificate details in the submission template you uploaded. There were 1 companies the SAO was responsible for during the financial year.",
    "If the information listed is not correct, upload an updated submission template before continuing.",
    "In accordance with Paragraph 2 Schedule 46 Finance Act 2009, I Steve the Senior Accounting Officer hereby certify, in respect of the financial year ended 31 December 1996 that 1 companies had appropriate tax accounting arrangements throughout the year."
  )

  val paragraphsWithNoData: Seq[String] = Seq(
    "This list is taken from the certificate details in the submission template you uploaded. There were 0 companies the SAO was responsible for during the financial year.",
    "If the information listed is not correct, upload an updated submission template before continuing.",
    "In accordance with Paragraph 2 Schedule 46 Finance Act 2009, I the Senior Accounting Officer hereby certify, in respect of the financial year ended 31 December that 0 companies had appropriate tax accounting arrangements throughout the year."
  )
}

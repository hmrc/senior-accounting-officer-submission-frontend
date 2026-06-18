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

  private def generateView(saoName: String, qualifiedCompanies: Seq[QualifiedCompany], companyCount: Int): Document =
    Jsoup.parse(SUT(saoName, qualifiedCompanies, companyCount).toString)

  "CertificateReviewQualifiedView" - {
    "When no rows are passed to the view must render empty table" - {
      val doc: Document = generateView(firstSaoName, Seq(), 0)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithOrWithoutError(hasError = false)

      doc.createTestsWithParagraphs(
        Seq(firstParagraphZeroCompanies, secondParagraph, thirdParagraphZeroQualifiedCompanies)
      )

      doc
        .select(secondParagraphLinkSelector)
        .get(0)
        .createTestWithLink(secondParagraphLinkText, routes.CertificateUploadFormController.onPageLoad().url)

      // TODO: test when no rows passed in

      doc.createTestsWithSubmissionButton(
        action = routes.CertificateReviewQualifiedController.onSubmit(),
        buttonText = "Continue"
      )
    }

    "When rows are passed to the view must render populated table" - {
      val doc: Document = generateView(firstSaoName, Seq(), 2)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithOrWithoutError(hasError = false)

      doc.createTestsWithParagraphs(
        Seq(firstParagraphTwoCompanies, secondParagraph, thirdParagraphTwoQualifiedCompanies)
      )

      doc
        .select(secondParagraphLinkSelector)
        .get(0)
        .createTestWithLink(secondParagraphLinkText, routes.CertificateUploadFormController.onPageLoad().url)

      // TODO: check bold

      // TODO: test the table

      doc.createTestsWithSubmissionButton(
        action = routes.CertificateReviewQualifiedController.onSubmit(),
        buttonText = "Continue"
      )
    }

    "When rows are and a different sao name are passed to the view must render populated table with differnt sao name" - {
      val doc: Document = generateView(secondSaoName, Seq(), 2)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithOrWithoutError(hasError = false)

      doc.createTestsWithParagraphs(
        Seq(firstParagraphTwoCompanies, secondParagraph, thirdParagraphTwoQualifiedCompaniesDifferentSao)
      )

      doc
        .select(secondParagraphLinkSelector)
        .get(0)
        .createTestWithLink(secondParagraphLinkText, routes.CertificateUploadFormController.onPageLoad().url)

      // TODO: check bold

      // TODO: test the table

      doc.createTestsWithSubmissionButton(
        action = routes.CertificateReviewQualifiedController.onSubmit(),
        buttonText = "Continue"
      )
    }
  }

  extension (doc: Document) {
    def createTestsWithQualifiedCompanyTable(): Unit = {
      // TODO: make a test case class to hold view info

      // TODO: test number of companies shown

      // TODO: test structure of html

    }
  }
}

object CertificateReviewQualifiedViewSpec {
  val pageHeading                 = "Review the companies with a qualified certificate"
  val pageTitle                   = "Review the companies with a qualified certificate"
  val firstParagraphZeroCompanies =
    "This list is taken from the certificate details in the submission template you uploaded. There were 0 companies the SAO was responsible for during the financial year."
  val firstParagraphTwoCompanies =
    "This list is taken from the certificate details in the submission template you uploaded. There were 2 companies the SAO was responsible for during the financial year."
  val secondParagraph =
    "If the information listed is not correct, upload an updated submission template before continuing."
  val thirdParagraphZeroQualifiedCompanies =
    "In accordance with paragraph 2 of Schedule 46 to the Finance Act 2009, I Firstname Lastname, the Senior Accounting Officer, hereby certify that throughout the company’s financial year ended 31 December 2024, 0 companies did not have appropriate tax accounting arrangements."
  val thirdParagraphTwoQualifiedCompanies =
    "In accordance with paragraph 2 of Schedule 46 to the Finance Act 2009, I Firstname Lastname, the Senior Accounting Officer, hereby certify that throughout the company’s financial year ended 31 December 2024, 0 companies did not have appropriate tax accounting arrangements."
  val thirdParagraphTwoQualifiedCompaniesDifferentSao =
    "In accordance with paragraph 2 of Schedule 46 to the Finance Act 2009, I Firstname Lastname II, the Senior Accounting Officer, hereby certify that throughout the company’s financial year ended 31 December 2024, 0 companies did not have appropriate tax accounting arrangements."
  val firstSaoName                = "Firstname Lastname"
  val secondSaoName               = "Firstname Lastname II"
  val secondParagraphLinkText     = "upload an updated submission template"
  val secondParagraphLinkSelector = ".govuk-body:nth-of-type(2) .govuk-link"
}

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
import models.displayRegimes

class CertificateReviewQualifiedViewSpec extends ViewSpecBase[CertificateReviewQualifiedView] {

  private def generateView(
      saoName: String,
      financialYearEnd: String,
      qualifiedCompanies: Seq[QualifiedCompany],
      companyCount: Int
  ): Document =
    Jsoup.parse(SUT(saoName, financialYearEnd, companyCount, qualifiedCompanies).toString)

  "CertificateReviewQualifiedView" - {
    "When no rows are passed to the view must render empty table" - {
      val doc: Document = generateView(firstSaoName, financialYearEnd, Seq(), 0)

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

      "must have bold text in second paragraph denoting number of qualified companies" in {
        doc.select("b").get(0).text() mustBe zeroQualifiedCompanyCountText
      }

      doc.createTestsWithQualifiedCompanyDescriptionList(Seq())

      doc.createTestsWithSubmissionButton(
        action = routes.CertificateReviewQualifiedController.onSubmit(),
        buttonText = "Continue"
      )
    }

    "When rows are passed to the view must render populated table" - {
      val doc: Document = generateView(firstSaoName, financialYearEnd, qualifiedCompanies, 2)

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

      "must have bold text in second paragraph denoting number of qualified companies" in {
        doc.select("b").get(0).text() mustBe twoQualifiedCompanyCountText
      }

      doc.createTestsWithQualifiedCompanyDescriptionList(qualifiedCompanies)

      doc.createTestsWithSubmissionButton(
        action = routes.CertificateReviewQualifiedController.onSubmit(),
        buttonText = "Continue"
      )
    }

    "When rows are and a different sao name are passed to the view must render populated table with differnt sao name" - {
      val doc: Document = generateView(secondSaoName, financialYearEnd, qualifiedCompanies, 2)

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

      "must have bold text in second paragraph denoting number of qualified companies" in {
        doc.select("b").get(0).text() mustBe twoQualifiedCompanyCountText
      }

      doc.createTestsWithQualifiedCompanyDescriptionList(qualifiedCompanies)

      doc.createTestsWithSubmissionButton(
        action = routes.CertificateReviewQualifiedController.onSubmit(),
        buttonText = "Continue"
      )
    }
  }

  extension (doc: Document) {
    def createTestsWithQualifiedCompanyDescriptionList(qualifiedCompanies: Seq[QualifiedCompany]): Unit = {
      val expectedCountOfDesciptionLists = qualifiedCompanies.size

      "must be one description list per qualified company" in {
        val descriptionLists = doc.select("dl.govuk-summary-list")
        descriptionLists.size() mustBe expectedCountOfDesciptionLists
      }

      "must be four description terms per qualified company with expected text" in {
        val descriptionTerms = doc.select("div.govuk-summary-list__row > dt.govuk-summary-list__key")

        descriptionTerms.size() mustBe expectedCountOfDesciptionLists * 4

        for i <- 0 to qualifiedCompanies.size - 1 do {
          descriptionTerms.get(i * 4).text() mustBe companyNameDescriptionTermText
          descriptionTerms.get(i * 4 + 1).text() mustBe utrDescriptionTermText
          descriptionTerms.get(i * 4 + 2).text() mustBe taxRegimesDescriptionTermText
          descriptionTerms.get(i * 4 + 3).text() mustBe additionalInformationDescriptionTermText
        }
      }

      "must be four description details per qualified company with expected text" in {
        val descriptionDetails = doc.select("div.govuk-summary-list__row > dd.govuk-summary-list__value")
        descriptionDetails.size() mustBe expectedCountOfDesciptionLists * 4

        for i <- 0 to qualifiedCompanies.size - 1 do {
          descriptionDetails.get(i * 4).text() mustBe qualifiedCompanies(i).name
          descriptionDetails.get(i * 4 + 1).text() mustBe qualifiedCompanies(i).utr
          descriptionDetails.get(i * 4 + 2).text() mustBe qualifiedCompanies(i).displayRegimes
          descriptionDetails.get(i * 4 + 3).text() mustBe qualifiedCompanies(i).additionalInformation
        }
      }
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
    "In accordance with paragraph 2 of Schedule 46 to the Finance Act 2009, I Firstname Lastname, the Senior Accounting Officer, hereby certify that throughout the company’s financial year ended 31 December 2024, 2 companies did not have appropriate tax accounting arrangements."
  val thirdParagraphTwoQualifiedCompaniesDifferentSao =
    "In accordance with paragraph 2 of Schedule 46 to the Finance Act 2009, I Firstname Lastname II, the Senior Accounting Officer, hereby certify that throughout the company’s financial year ended 31 December 2024, 2 companies did not have appropriate tax accounting arrangements."
  val firstSaoName                  = "Firstname Lastname"
  val secondSaoName                 = "Firstname Lastname II"
  val financialYearEnd              = "31 December 2024"
  val zeroQualifiedCompanyCountText = "0"
  val twoQualifiedCompanyCountText  = "2"
  val secondParagraphLinkText       = "upload an updated submission template"
  val secondParagraphLinkSelector   = ".govuk-body:nth-of-type(2) .govuk-link"

  val qualifiedCompanies = Seq(
    QualifiedCompany(
      name = "example company name",
      utr = "example company utr",
      corporationTax = false,
      valueAddedTax = true,
      paye = false,
      insurancePremiumTax = true,
      stampDutyLandTax = false,
      stampDutyReserveTax = false,
      petroleumRevenueTax = true,
      customsDuties = false,
      exciseDuties = false,
      bankLevy = false,
      additionalInformation = "example additional information"
    ),
    QualifiedCompany(
      name = "example company name 2",
      utr = "example company utr 2",
      corporationTax = false,
      valueAddedTax = true,
      paye = false,
      insurancePremiumTax = true,
      stampDutyLandTax = false,
      stampDutyReserveTax = true,
      petroleumRevenueTax = false,
      customsDuties = false,
      exciseDuties = true,
      bankLevy = false,
      additionalInformation = "example additional information 2"
    )
  )

  val companyNameDescriptionTermText           = "Company name"
  val utrDescriptionTermText                   = "UTR"
  val taxRegimesDescriptionTermText            = "Tax Regimes"
  val additionalInformationDescriptionTermText = "Additional information"
}

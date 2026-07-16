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
import controllers.certificate.routes as certificateRoutes
import models.{QualifiedCompany, displayRegimes}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.certificate.CertificateReviewQualifiedView

import java.time.LocalDate

import CertificateReviewQualifiedViewSpec.*

class CertificateReviewQualifiedViewSpec extends ViewSpecBase[CertificateReviewQualifiedView] {

  private def generateView(
      saoName: String,
      qualifiedCompanies: Seq[QualifiedCompany],
      companyCount: Int
  ): Document =
    Jsoup.parse(SUT(saoName, companyCount, qualifiedCompanies).toString)

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
        .createTestWithLink(secondParagraphLinkText, certificateRoutes.CertificateUploadFormController.onPageLoad().url)

      "must have bold text in third paragraph denoting number of qualified companies" in {
        doc.select("b").get(0).text() mustBe zeroQualifiedCompanyCountText
      }

      doc.createTestsWithQualifiedCompanyDescriptionList(Seq())

      doc.createTestsWithSubmissionButton(
        action = certificateRoutes.CertificateReviewQualifiedController.onSubmit(),
        buttonText = "Continue"
      )
    }

    "When rows are passed to the view must render populated table" - {
      val doc: Document = generateView(firstSaoName, qualifiedCompanies, 2)

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
        .createTestWithLink(secondParagraphLinkText, certificateRoutes.CertificateUploadFormController.onPageLoad().url)

      "must have bold text in third paragraph denoting number of qualified companies" in {
        doc.select("b").get(0).text() mustBe twoQualifiedCompanyCountText
      }

      doc.createTestsWithQualifiedCompanyDescriptionList(qualifiedCompanies)

      doc.createTestsWithSubmissionButton(
        action = certificateRoutes.CertificateReviewQualifiedController.onSubmit(),
        buttonText = "Continue"
      )
    }

    "When rows are and a different sao name are passed to the view must render populated table with differnt sao name" - {
      val doc: Document = generateView(secondSaoName, qualifiedCompanies, 2)

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
        .createTestWithLink(secondParagraphLinkText, certificateRoutes.CertificateUploadFormController.onPageLoad().url)

      "must have bold text in third paragraph denoting number of qualified companies" in {
        doc.select("b").get(0).text() mustBe twoQualifiedCompanyCountText
      }

      doc.createTestsWithQualifiedCompanyDescriptionList(qualifiedCompanies)

      doc.createTestsWithSubmissionButton(
        action = certificateRoutes.CertificateReviewQualifiedController.onSubmit(),
        buttonText = "Continue"
      )
    }
  }

  extension (doc: Document) {
    def createTestsWithQualifiedCompanyDescriptionList(qualifiedCompanies: Seq[QualifiedCompany]): Unit = {
      val expectedCountOfDescriptionLists = qualifiedCompanies.size

      "must be one description list per qualified company" in {
        val descriptionLists = doc.select("dl.govuk-summary-list")
        descriptionLists.size() mustBe expectedCountOfDescriptionLists
      }

      "must be four description terms per qualified company with expected text" in {
        val descriptionTerms = doc.select("div.govuk-summary-list__row > dt.govuk-summary-list__key")

        descriptionTerms.size() mustBe expectedCountOfDescriptionLists * 8

        for i <- qualifiedCompanies.indices do {
          descriptionTerms.get(i * 8).text() mustBe companyNameDescriptionTermText
          descriptionTerms.get(i * 8 + 1).text() mustBe utrDescriptionTermText
          descriptionTerms.get(i * 8 + 2).text() mustBe crnDescriptionTermText
          descriptionTerms.get(i * 8 + 3).text() mustBe typeDescriptionTermText
          descriptionTerms.get(i * 8 + 4).text() mustBe statusDescriptionTermText
          descriptionTerms.get(i * 8 + 5).text() mustBe financialYearEndDescriptionTermText
          descriptionTerms.get(i * 8 + 6).text() mustBe taxRegimesDescriptionTermText
          descriptionTerms.get(i * 8 + 7).text() mustBe additionalInformationDescriptionTermText
        }
      }

      "must be four description details per qualified company with expected text" in {
        val descriptionDetails = doc.select("div.govuk-summary-list__row > dd.govuk-summary-list__value")
        descriptionDetails.size() mustBe expectedCountOfDescriptionLists * 8

        for i <- qualifiedCompanies.indices do {
          descriptionDetails.get(i * 8).text() mustBe qualifiedCompanies(i).name
          descriptionDetails.get(i * 8 + 1).text() mustBe qualifiedCompanies(i).utr
          descriptionDetails.get(i * 8 + 2).text() mustBe qualifiedCompanies(i).crn.fold("Not provided")(identity)
          descriptionDetails.get(i * 8 + 3).text() mustBe qualifiedCompanies(i).companyType
          descriptionDetails.get(i * 8 + 4).text() mustBe qualifiedCompanies(i).status
          descriptionDetails.get(i * 8 + 5).text() mustBe testDateAsString(qualifiedCompanies(i).financialYearEndDate)
          descriptionDetails.get(i * 8 + 6).text() mustBe qualifiedCompanies(i).displayRegimes
          descriptionDetails.get(i * 8 + 7).text() mustBe qualifiedCompanies(i).additionalInformation
        }
      }
    }
  }
}

object CertificateReviewQualifiedViewSpec {
  val pageHeading                 = "Review the companies with a qualified certificate"
  val pageTitle                   = "Review the companies with a qualified certificate"
  val firstParagraphZeroCompanies =
    "This list is from the certificate details in your submission template. There were 0 companies your SAO was responsible for in a previous financial year."
  val firstParagraphTwoCompanies =
    "This list is from the certificate details in your submission template. There were 2 companies your SAO was responsible for in a previous financial year."
  val secondParagraph =
    "If any companies listed are missing or incorrect, upload an updated submission template before continuing."
  val thirdParagraphZeroQualifiedCompanies =
    "In accordance with paragraph 2, Schedule 46 of the Finance Act 2009, I Firstname Lastname, the Senior Accounting Officer, hereby certify that 0 companies did not have appropriate tax accounting arrangements."
  val thirdParagraphTwoQualifiedCompanies =
    "In accordance with paragraph 2, Schedule 46 of the Finance Act 2009, I Firstname Lastname, the Senior Accounting Officer, hereby certify that 2 companies did not have appropriate tax accounting arrangements."
  val thirdParagraphTwoQualifiedCompaniesDifferentSao =
    "In accordance with paragraph 2, Schedule 46 of the Finance Act 2009, I Firstname Lastname II, the Senior Accounting Officer, hereby certify that 2 companies did not have appropriate tax accounting arrangements."
  val firstSaoName                  = "Firstname Lastname"
  val secondSaoName                 = "Firstname Lastname II"
  val zeroQualifiedCompanyCountText = "0"
  val twoQualifiedCompanyCountText  = "2"
  val secondParagraphLinkText       = "upload an updated submission template"
  val secondParagraphLinkSelector   = ".govuk-body:nth-of-type(2) .govuk-link"

  val testDate1: LocalDate = LocalDate.of(2026, 1, 1)
  val testDate2: LocalDate = LocalDate.of(2027, 2, 3)

  def testDateAsString(localDate: LocalDate): String =
    Map(
      testDate1 -> "1 January 2026",
      testDate2 -> "3 February 2027"
    ).get(localDate).fold(throw RuntimeException("Unknown test date"))(identity)

  val qualifiedCompanies: Seq[QualifiedCompany] = Seq(
    QualifiedCompany(
      name = "example company name",
      utr = "example company utr",
      crn = Some("example company crn"),
      companyType = "LTD",
      status = "Active",
      financialYearEndDate = testDate1,
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
      crn = None,
      companyType = "PLC",
      status = "Active",
      financialYearEndDate = testDate2,
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
  val crnDescriptionTermText                   = "CRN"
  val typeDescriptionTermText                  = "Type"
  val statusDescriptionTermText                = "Status"
  val financialYearEndDescriptionTermText      = "Financial year end"
  val taxRegimesDescriptionTermText            = "Tax Regimes"
  val additionalInformationDescriptionTermText = "Explain why the certificate is qualified"
}

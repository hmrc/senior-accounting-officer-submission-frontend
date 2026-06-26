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
import models.UnqualifiedCompany
import models.upload.{CompanyStatus, CompanyType}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.certificate.CertificateReviewUnqualifiedView

import CertificateReviewUnqualifiedViewSpec.*

class CertificateReviewUnqualifiedViewSpec extends ViewSpecBase[CertificateReviewUnqualifiedView] {
  private def generateView(
      saoName: String,
      unqualifiedCompanies: Seq[UnqualifiedCompany],
      companyCount: Int,
      dummyDate: String
  ): Document = Jsoup.parse(SUT(saoName, unqualifiedCompanies, companyCount, dummyDate).toString)

  "CertificateReviewUnqualifiedView" - {
    "will populate the table correctly when data is present" - {

      val doc: Document = generateView(saoName, unqualifiedCompanies, unqualifiedCompanies.size, dummyDate)

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
        action = certificateRoutes.CertificateReviewUnqualifiedController.onSubmit(),
        buttonText = "Continue"
      )

      "must have bold text in third paragraph denoting number of qualified companies" in {
        doc.select("b").get(0).text() mustBe "2"
      }

      doc
        .select(linkLocator)
        .get(0)
        .createTestWithLink(linkText, certificateRoutes.CertificateUploadFormController.onPageLoad().url)

      doc.createTestsWithUnqualifiedCompanyDescriptionList(unqualifiedCompanies)

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
        action = certificateRoutes.CertificateReviewUnqualifiedController.onSubmit(),
        buttonText = "Continue"
      )

      "must have bold text in third paragraph denoting number of qualified companies" in {
        doc.select("b").get(0).text() mustBe "0"
      }

      doc
        .select(linkLocator)
        .get(0)
        .createTestWithLink(linkText, certificateRoutes.CertificateUploadFormController.onPageLoad().url)

      doc.createTestsWithUnqualifiedCompanyDescriptionList(Seq())

    }
  }
  extension (doc: Document) {
    def createTestsWithUnqualifiedCompanyDescriptionList(unqualifiedCompanies: Seq[UnqualifiedCompany]): Unit = {
      val expectedCountOfDescriptionLists = unqualifiedCompanies.size

      "must be one description list per unqualified company" in {
        val descriptionLists = doc.select("dl.govuk-summary-list")
        descriptionLists.size() mustBe expectedCountOfDescriptionLists
      }

      "must be five description terms per unqualified company with expected text" in {
        val descriptionTerms = doc.select("div.govuk-summary-list__row > dt.govuk-summary-list__key")

        descriptionTerms.size() mustBe expectedCountOfDescriptionLists * 5

        for i <- 0 to unqualifiedCompanies.size - 1 do {
          descriptionTerms.get(i * 5).text() mustBe companyNameDescriptionTermText
          descriptionTerms.get(i * 5 + 1).text() mustBe utrDescriptionTermText
          descriptionTerms.get(i * 5 + 2).text() mustBe crnDescriptionTermText
          descriptionTerms.get(i * 5 + 3).text() mustBe typeDescriptionTermText
          descriptionTerms.get(i * 5 + 4).text() mustBe statusDescriptionTermText
        }
      }

      "must be five description details per unqualified company with expected text" in {
        val descriptionDetails = doc.select("div.govuk-summary-list__row > dd.govuk-summary-list__value")
        descriptionDetails.size() mustBe expectedCountOfDescriptionLists * 5

        for i <- 0 to unqualifiedCompanies.size - 1 do {
          descriptionDetails.get(i * 5).text() mustBe unqualifiedCompanies(i).name
          descriptionDetails.get(i * 5 + 1).text() mustBe unqualifiedCompanies(i).utr
          descriptionDetails.get(i * 5 + 2).text() mustBe unqualifiedCompanies(i).crn
          descriptionDetails.get(i * 5 + 3).text() mustBe unqualifiedCompanies(i).companyType.toString
          descriptionDetails.get(i * 5 + 4).text() mustBe unqualifiedCompanies(i).companyStatus.toString
        }
      }
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
    "This list is taken from the certificate details in the submission template you uploaded. There were 2 companies the SAO was responsible for during the financial year.",
    "If the information listed is not correct, upload an updated submission template before continuing.",
    "In accordance with Paragraph 2 Schedule 46 Finance Act 2009, I example sao name the Senior Accounting Officer hereby certify, in respect of the financial year ended 31 December 1999 that 2 companies had appropriate tax accounting arrangements throughout the year."
  )

  val paragraphsWithNoData: Seq[String] = Seq(
    "This list is taken from the certificate details in the submission template you uploaded. There were 0 companies the SAO was responsible for during the financial year.",
    "If the information listed is not correct, upload an updated submission template before continuing.",
    "In accordance with Paragraph 2 Schedule 46 Finance Act 2009, I the Senior Accounting Officer hereby certify, in respect of the financial year ended 31 December that 0 companies had appropriate tax accounting arrangements throughout the year."
  )

  val unqualifiedCompanies: Seq[UnqualifiedCompany] = Seq(
    UnqualifiedCompany(
      name = "example company name",
      utr = "example utr",
      crn = "example crn",
      companyType = CompanyType.LTD,
      companyStatus = CompanyStatus.Active
    ),
    UnqualifiedCompany(
      name = "example company name 2",
      utr = "example utr 2",
      crn = "example crn 2",
      companyType = CompanyType.PLC,
      companyStatus = CompanyStatus.Dormant
    )
  )

  val companyNameDescriptionTermText = "Company name"
  val utrDescriptionTermText         = "UTR"
  val crnDescriptionTermText         = "CRN"
  val typeDescriptionTermText        = "Type"
  val statusDescriptionTermText      = "Status"

  val saoName   = "example sao name"
  val dummyDate = "1999"

}

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
import base.ViewSpecBase.excludeHelpLinkAndErrorMessageParagraphsSelector
import controllers.certificate.routes as certificateRoutes
import models.QualifiedCompany
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.certificate.CertificateReviewQualifiedView

import java.time.LocalDate

import CertificateReviewQualifiedViewSpec.*

class CertificateReviewQualifiedViewSpec extends ViewSpecBase[CertificateReviewQualifiedView] {

  private def generateView(
      saoName: String,
      qualifiedCompanies: Seq[QualifiedCompany],
      companyCount: Int,
      totalQualifiedCompanies: Int = 0,
      currentPage: Int = 1,
      pageCount: Int = 1
  ): Document =
    Jsoup.parse(
      SUT(
        saoName,
        companyCount,
        qualifiedCompanies,
        totalQualifiedCompanies,
        currentPage,
        pageCount
      ).toString
    )

  "CertificateReviewQualifiedView" - {
    "when no rows are passed to the view must render an empty table" - {
      val doc: Document =
        generateView(firstSaoName, Seq.empty, 0, totalQualifiedCompanies = 0)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithCaption(pageCaption)
      doc.createTestsWithOrWithoutError(hasError = false)

      doc.createTestsWithParagraphs(
        Seq(firstParagraphZeroCompanies, secondParagraph, thirdParagraphZeroQualifiedCompanies)
      )

      doc.createTestForHorizontalLine()

      doc
        .select(secondParagraphLinkSelector)
        .get(0)
        .createTestWithLink(
          linkTestDescription = "upload updated submission",
          linkText = secondParagraphLinkText,
          destinationUrl = certificateRoutes.CertificateUploadFormController.onPageLoad().url,
          isExternalLink = false
        )

      doc.createTestsWithQualifiedCompanyTable(Seq.empty)

      doc.createTestsWithSubmissionButton(
        action = certificateRoutes.CertificateReviewQualifiedController.onSubmit(),
        buttonText = "Continue"
      )
    }

    "when rows are passed to the view must render a populated table" - {
      val doc: Document =
        generateView(firstSaoName, qualifiedCompanies, 2, totalQualifiedCompanies = 2)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithOrWithoutError(hasError = false)
      doc.createTestsWithCaption(pageCaption)
      doc.createTestForHorizontalLine()

      doc.createTestsWithParagraphs(
        Seq(firstParagraphTwoCompanies, secondParagraph, thirdParagraphTwoQualifiedCompanies)
      )

      doc
        .select(secondParagraphLinkSelector)
        .get(0)
        .createTestWithLink(
          linkTestDescription = "upload updated submission",
          linkText = secondParagraphLinkText,
          destinationUrl = certificateRoutes.CertificateUploadFormController.onPageLoad().url,
          isExternalLink = false
        )

      doc.createTestsWithQualifiedCompanyTable(qualifiedCompanies)

      doc.createTestsWithSubmissionButton(
        action = certificateRoutes.CertificateReviewQualifiedController.onSubmit(),
        buttonText = "Continue"
      )
    }

    "when one company is passed to the view must render singular wording" - {
      val doc =
        generateView(
          firstSaoName,
          qualifiedCompanies.take(1),
          1,
          totalQualifiedCompanies = 1
        )

      doc.createTestsWithParagraphs(
        Seq(firstParagraphOneCompany, secondParagraph, thirdParagraphOneQualifiedCompany)
      )
    }

    "when rows and a different SAO name are passed, must render the different SAO name" - {
      val doc: Document =
        generateView(secondSaoName, qualifiedCompanies, 2, totalQualifiedCompanies = 2)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithOrWithoutError(hasError = false)
      doc.createTestsWithCaption(pageCaption)
      doc.createTestForHorizontalLine()

      doc.createTestsWithParagraphs(
        Seq(
          firstParagraphTwoCompanies,
          secondParagraph,
          thirdParagraphTwoQualifiedCompaniesDifferentSao
        )
      )

      doc
        .select(secondParagraphLinkSelector)
        .get(0)
        .createTestWithLink(
          linkTestDescription = "upload updated submission",
          linkText = secondParagraphLinkText,
          destinationUrl = certificateRoutes.CertificateUploadFormController.onPageLoad().url,
          isExternalLink = false
        )

      doc.createTestsWithQualifiedCompanyTable(qualifiedCompanies)

      doc.createTestsWithSubmissionButton(
        action = certificateRoutes.CertificateReviewQualifiedController.onSubmit(),
        buttonText = "Continue"
      )
    }

    "the content of the SAO name is properly escaped" in {
      val unescapedText = "<script>alert(123)</script>"
      val doc: Document = generateView(unescapedText, qualifiedCompanies, 2, totalQualifiedCompanies = 2)

      doc
        .select(excludeHelpLinkAndErrorMessageParagraphsSelector)
        .eachText()
        .toArray
        .toSeq
        .mkString(",") must include(unescapedText)
    }

    "renders pagination when there is more than one page" in {
      val doc: Document =
        generateView(
          firstSaoName,
          qualifiedCompanies.take(1),
          companyCount = 30,
          totalQualifiedCompanies = 30,
          currentPage = 1,
          pageCount = 2
        )

      doc.select(".govuk-pagination").size() mustBe 1
      doc.select(".govuk-pagination__next").size() mustBe 1
      doc.select(".govuk-pagination__prev").size() mustBe 0
    }
  }

  extension (doc: Document) {

    def createTestForHorizontalLine(): Unit = {
      val hr     = doc.select("hr")
      val styles = hr.eachAttr("style")

      styles.size mustBe 1
      styles.get(0) mustBe hrStyle
      hr.size mustBe 1
    }

    def createTestsWithQualifiedCompanyTable(
        qualifiedCompanies: Seq[QualifiedCompany]
    ): Unit = {
      "must render one table" in {
        doc.select("table.govuk-table").size() mustBe 1
      }

      "must render the expected table headers" in {
        doc.select("table.govuk-table thead th").eachText().toArray.toSeq mustBe Seq(
          companyNameDescriptionTermText,
          crnDescriptionTermText,
          utrDescriptionTermText,
          typeDescriptionTermText,
          statusDescriptionTermText,
          financialYearEndDescriptionTermText
        )
      }

      "must render one table row per qualified company" in {
        val rows = doc.select("table.govuk-table tbody tr")
        rows.size() mustBe qualifiedCompanies.size

        for i <- qualifiedCompanies.indices do {
          val cells   = rows.get(i).select("td").eachText().toArray.toSeq
          val company = qualifiedCompanies(i)

          cells mustBe Seq(
            company.name,
            company.crn.getOrElse("Not provided"),
            company.utr,
            company.companyType,
            company.status,
            testDateAsString(company.financialYearEndDate)
          )
        }
      }
    }
  }
}

object CertificateReviewQualifiedViewSpec {
  val pageHeading = "Review the companies with a qualified certificate"
  val pageCaption = "Submit a certificate"
  val pageTitle   = "Review the companies with a qualified certificate - Submit a certificate"
  val hrStyle     = "border: 0; border-top: 1px solid; color: var(--govuk-border-colour,#cecece);"

  val firstParagraphZeroCompanies =
    "This list is from the certificate details in your submission template. There were 0 companies the SAO was responsible for in a previous financial year."
  val firstParagraphTwoCompanies =
    "This list is from the certificate details in your submission template. There were 2 companies the SAO was responsible for in a previous financial year."
  val firstParagraphOneCompany =
    "This list is from the certificate details in your submission template. There was 1 company the SAO was responsible for in a previous financial year."

  val secondParagraph =
    "If any company’s details are missing or incorrect, upload an updated submission template before continuing."

  val thirdParagraphZeroQualifiedCompanies =
    "In accordance with paragraph 2, Schedule 46 of the Finance Act 2009, I Firstname Lastname, the Senior Accounting Officer, hereby certify that 0 companies did not have appropriate tax accounting arrangements."
  val thirdParagraphTwoQualifiedCompanies =
    "In accordance with paragraph 2, Schedule 46 of the Finance Act 2009, I Firstname Lastname, the Senior Accounting Officer, hereby certify that 2 companies did not have appropriate tax accounting arrangements."
  val thirdParagraphOneQualifiedCompany =
    "In accordance with paragraph 2, Schedule 46 of the Finance Act 2009, I Firstname Lastname, the Senior Accounting Officer, hereby certify that 1 company did not have appropriate tax accounting arrangements."
  val thirdParagraphTwoQualifiedCompaniesDifferentSao =
    "In accordance with paragraph 2, Schedule 46 of the Finance Act 2009, I Firstname Lastname II, the Senior Accounting Officer, hereby certify that 2 companies did not have appropriate tax accounting arrangements."

  val firstSaoName                = "Firstname Lastname"
  val secondSaoName               = "Firstname Lastname II"
  val secondParagraphLinkText     = "upload an updated submission template"
  val secondParagraphLinkSelector = ".govuk-body:nth-of-type(2) .govuk-link"

  val testDate1: LocalDate = LocalDate.of(2026, 1, 1)
  val testDate2: LocalDate = LocalDate.of(2027, 2, 3)

  def testDateAsString(localDate: LocalDate): String =
    Map(
      testDate1 -> "1 January 2026",
      testDate2 -> "3 February 2027"
    ).getOrElse(localDate, throw new RuntimeException("Unknown test date"))

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

  val companyNameDescriptionTermText      = "Company name"
  val utrDescriptionTermText              = "UTR"
  val crnDescriptionTermText              = "CRN"
  val typeDescriptionTermText             = "Type"
  val statusDescriptionTermText           = "Status"
  val financialYearEndDescriptionTermText = "Financial year end"
}

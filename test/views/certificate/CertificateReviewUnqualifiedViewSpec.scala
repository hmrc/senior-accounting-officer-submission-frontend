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
import models.UnqualifiedCompany
import models.upload.{CompanyStatus, CompanyType}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.certificate.CertificateReviewUnqualifiedView

import java.time.LocalDate

import CertificateReviewUnqualifiedViewSpec.*

class CertificateReviewUnqualifiedViewSpec extends ViewSpecBase[CertificateReviewUnqualifiedView] {
  private def generateView(
      saoName: String,
      unqualifiedCompanies: Seq[UnqualifiedCompany],
      companyCount: Int,
      totalUnqualifiedCompanies: Int = 0,
      currentPage: Int = 1,
      pageCount: Int = 1
  ): Document = Jsoup.parse(
    SUT(saoName, unqualifiedCompanies, companyCount, totalUnqualifiedCompanies, currentPage, pageCount).toString
  )

  "CertificateReviewUnqualifiedView" - {
    "will populate the table correctly when data is present" - {

      val doc: Document = generateView(saoName, unqualifiedCompanies, 10, totalUnqualifiedCompanies = 2)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithCaption(pageCaption)

      doc.createTestsWithOrWithoutError(hasError = false)

      doc.createTestsWithParagraphs(paragraphs)

      doc.createTestsWithSubmissionButton(
        action = certificateRoutes.CertificateReviewUnqualifiedController.onSubmit(),
        buttonText = "Continue"
      )

      doc
        .select(linkLocator)
        .get(0)
        .createTestWithLink(
          linkTestDescription = "upload updated submission template",
          linkText = linkText,
          destinationUrl = certificateRoutes.CertificateUploadFormController.onPageLoad().url,
          isExternalLink = false
        )

      doc.createTestsWithUnqualifiedCompanyTable(unqualifiedCompanies)

    }

    "will populate the table correctly when no data is present" - {

      val doc: Document = generateView("", Seq.empty, 0, totalUnqualifiedCompanies = 0)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithCaption(pageCaption)

      doc.createTestsWithOrWithoutError(hasError = false)

      doc.createTestsWithParagraphs(paragraphsWithNoData)

      doc.createTestsWithSubmissionButton(
        action = certificateRoutes.CertificateReviewUnqualifiedController.onSubmit(),
        buttonText = "Continue"
      )

      doc
        .select(linkLocator)
        .get(0)
        .createTestWithLink(
          linkTestDescription = "update uploaded submission template",
          linkText = linkText,
          destinationUrl = certificateRoutes.CertificateUploadFormController.onPageLoad().url,
          isExternalLink = false
        )

      doc.createTestsWithUnqualifiedCompanyTable(Seq())

    }

    "will use singular wording when one company is present" - {
      val doc = generateView(saoName, unqualifiedCompanies.take(1), 1, totalUnqualifiedCompanies = 1)

      doc.createTestsWithParagraphs(paragraphsWithOneCompany)

    }

    "The content of SAO's name is properly escaped" in {
      val unescapedText = "<script>alert(123)</script>"
      val doc: Document = generateView(unescapedText, unqualifiedCompanies, 2)

      doc.select(excludeHelpLinkAndErrorMessageParagraphsSelector).eachText().toArray.toSeq.mkString(",") must include(
        unescapedText
      )
    }

    "Will paginate correctly" in {
      val doc = generateView(
        saoName,
        unqualifiedCompanies,
        companyCount = 20,
        totalUnqualifiedCompanies = 20,
        currentPage = 1,
        pageCount = 2
      )

      doc.select(".govuk-pagination").size() mustBe 1
      doc.select(".govuk-pagination__next").size() mustBe 1
      doc.select(".govuk-pagination__prev").size() mustBe 0
    }
  }
  extension (doc: Document) {
    def createTestsWithUnqualifiedCompanyTable(
        unqualifiedCompanies: Seq[UnqualifiedCompany]
    ): Unit = {
      "must render one table" in {
        doc.select("table.govuk-table").size() mustBe 1
      }

      "must render the expected table headers" in {
        val headers = doc.select("table.govuk-table thead th").eachText().toArray.toSeq

        headers mustBe Seq(
          companyNameDescriptionTermText,
          crnDescriptionTermText,
          utrDescriptionTermText,
          typeDescriptionTermText,
          statusDescriptionTermText,
          financialYearEndDateDescriptionTermText
        )
      }

      "must render one table row per company" in {
        val rows = doc.select("table.govuk-table tbody tr")

        rows.size() mustBe unqualifiedCompanies.size

        for i <- unqualifiedCompanies.indices do {
          val cells   = rows.get(i).select("td").eachText().toArray.toSeq
          val company = unqualifiedCompanies(i)

          cells mustBe Seq(
            company.name,
            company.crn.fold("")(identity),
            company.utr,
            company.companyType.toString,
            company.companyStatus.toString,
            testDateAsString(company.financialYearEndDate)
          )
        }
      }
    }
  }
}

object CertificateReviewUnqualifiedViewSpec {
  val pageHeading             = "Review the companies with an unqualified certificate"
  val pageCaption             = "Submit a certificate"
  val pageTitle: String       = "Review the companies with an unqualified certificate - Submit a certificate"
  val linkLocator             = ".govuk-body:nth-of-type(2) .govuk-link"
  val linkText                = "upload an updated submission template"
  val paragraphs: Seq[String] = Seq(
    "This list is from the certificate details in your submission template. There were 10 companies the SAO was responsible for in a previous financial year.",
    "If any company’s details are missing or incorrect, upload an updated submission template before continuing.",
    "In accordance with Paragraph 2, Schedule 46 of the Finance Act 2009, I example sao name, the Senior Accounting Officer hereby certify that 2 companies had appropriate tax accounting arrangements throughout the year."
  )

  val paragraphsWithNoData: Seq[String] = Seq(
    "This list is from the certificate details in your submission template. There were 0 companies the SAO was responsible for in a previous financial year.",
    "If any company’s details are missing or incorrect, upload an updated submission template before continuing.",
    "In accordance with Paragraph 2, Schedule 46 of the Finance Act 2009, I , the Senior Accounting Officer hereby certify that 0 companies had appropriate tax accounting arrangements throughout the year."
  )

  val paragraphsWithOneCompany: Seq[String] = Seq(
    "This list is from the certificate details in your submission template. There was 1 company the SAO was responsible for in a previous financial year.",
    "If any company’s details are missing or incorrect, upload an updated submission template before continuing.",
    "In accordance with Paragraph 2, Schedule 46 of the Finance Act 2009, I example sao name, the Senior Accounting Officer hereby certify that 1 company had appropriate tax accounting arrangements throughout the year."
  )

  private val testDate1 = LocalDate.of(2026, 1, 1)
  private val testDate2 = LocalDate.of(2027, 3, 4)

  def testDateAsString(localDate: LocalDate): String =
    Map(
      testDate1 -> "1 January 2026",
      testDate2 -> "4 March 2027"
    ).get(localDate).fold(throw RuntimeException("Unknown test date"))(identity)

  val unqualifiedCompanies: Seq[UnqualifiedCompany] = Seq(
    UnqualifiedCompany(
      name = "example company name",
      utr = "example utr",
      crn = Some("example crn"),
      companyType = CompanyType.LTD,
      companyStatus = CompanyStatus.Active,
      financialYearEndDate = testDate1
    ),
    UnqualifiedCompany(
      name = "example company name 2",
      utr = "example utr 2",
      crn = Some("example crn 2"),
      companyType = CompanyType.PLC,
      companyStatus = CompanyStatus.Dormant,
      financialYearEndDate = testDate2
    )
  )

  val companyNameDescriptionTermText          = "Company name"
  val utrDescriptionTermText                  = "UTR"
  val crnDescriptionTermText                  = "CRN"
  val typeDescriptionTermText                 = "Type"
  val statusDescriptionTermText               = "Status"
  val financialYearEndDateDescriptionTermText = "Financial year end"

  val saoName = "example sao name"

}

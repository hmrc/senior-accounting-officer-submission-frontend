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
import models.upload.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.UploadTemplateTableErrorViewSpec.*
import views.html.UploadTemplateTableErrorView

import scala.jdk.CollectionConverters.*

class UploadTemplateTableErrorViewSpec extends ViewSpecBase[UploadTemplateTableErrorView] {

  private def generateView(): Document = Jsoup.parse(SUT(tableData).toString)

  "UploadTemplateTableErrorView" - {
    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = true,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    "must render error table columns and content" in {
      val headings = doc.select("th.govuk-table__header").eachText()
      headings must contain allOf ("Row number", "Column", "Errors to Correct")
      headings must not contain "Code"

      val tableRows = doc.select("tbody.govuk-table__body tr")
      tableRows.size() mustBe 2
      tableRows.first().select("td").first().text() mustBe "9"
      tableRows.first().select("td").first().attr("rowspan") mustBe "2"
      doc.select("tbody.govuk-table__body").text() must include("Enter a valid Company UTR. It must be 10 digits long")
      doc.select("tbody.govuk-table__body").text() must include(
        "Enter a valid Company CRN. It must be 8 characters long"
      )
    }

    "must render the problem summary and guidance link" in {
      doc.text() must include("Your file has 2 errors.")
      val link = doc.select("a.govuk-link").asScala.find(_.text().contains("Read guidance")).value
      link.attr("href") mustBe controllers.routes.TemplateGuidanceController.onPageLoad().url
      link.attr("target") mustBe "_blank"
    }

    "must render return to file upload button" in {
      doc.select("#continue").size() mustBe 1
      doc.select("#continue").text() mustBe "Return to file upload"
    }
  }
}

object UploadTemplateTableErrorViewSpec {
  val pageHeading = "There is a problem with your submission template file"
  val pageTitle   = "There is a problem with your submission template file"

  val tableData: UploadTemplateTableData = UploadTemplateTableData(
    rows = Seq.empty,
    errors = Seq(
      TemplateParseError(
        line = 9,
        column = Some("UTR"),
        code = "invalid_company_utr",
        message = "Enter a valid Company UTR. It must be 10 digits long"
      ),
      TemplateParseError(
        line = 9,
        column = Some("CRN"),
        code = "invalid_company_crn",
        message = "Enter a valid Company CRN. It must be 8 characters long"
      )
    )
  )
}

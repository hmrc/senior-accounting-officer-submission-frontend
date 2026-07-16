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
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import uk.gov.hmrc.govukfrontend.views.Implicits.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Value as SLValue, *}
import viewmodels.govuk.summarylist.*
import views.html.certificate.CertificateCheckYourAnswersView

import CertificateCheckYourAnswersViewSpec.*

class CertificateCheckYourAnswersViewSpec extends ViewSpecBase[CertificateCheckYourAnswersView] {

  private def generateView(summaryList: SummaryList): Document = Jsoup.parse(SUT(summaryList).toString)

  "CertificateCheckYourAnswersView" - {
    "empty summary list must result in no table rows" - {
      val summaryList   = SummaryList()
      val doc: Document = generateView(summaryList)

      "Summary Grid" - {
        "must be exactly one present" in {
          doc.summaryListGrid.size() mustBe 1
        }
      }

      "Description List" - {

        "must have description list" in {
          doc.descriptionLists.size() mustBe 1
        }

        "description list must have no rows" in {
          doc.descriptionList.getElementsByClass("govuk-summary-list__row").size() mustBe 0
        }

        "description list must have css class 'govuk-summary-list'" in {
          doc.descriptionList.hasClass("govuk-summary-list") mustBe true
        }
      }

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithOrWithoutError(hasError = false)

      doc.createTestsWithLargeCaption(
        pageCaption
      )

      doc.createTestsWithSubmissionButton(
        action = certificateRoutes.CertificateCheckYourAnswersController.onSubmit(),
        buttonText = pageButtonText
      )
    }

    "non-empty summary list must result in a table with rows" - {
      val summaryList =
        SummaryList(rows =
          Seq(
            SummaryListRowViewModel(
              key = testKey1.toKey,
              value = SLValue(testValue1.toText),
              actions = Seq(
                ActionItemViewModel(
                  testActionMessage1.toText,
                  testActionUrl1
                )
                  .withVisuallyHiddenText(testActionHiddenText1)
              )
            ),
            SummaryListRowViewModel(
              key = testKey2.toKey,
              value = SLValue(testValue2.toText)
            )
          )
        )

      val doc: Document = generateView(summaryList)

      "Summary Grid" - {
        "must be exactly one present" in {
          doc.summaryListGrid.size() mustBe 1
        }

        "page must have description list" in {
          doc.descriptionLists.size() mustBe 1
        }

        "description list" - {

          "must have two rows" in {
            doc.descriptionList.getElementsByClass("govuk-summary-list__row").size() mustBe 2
          }

          "must generate correct content when there is a call to action" in {
            validateSummaryListRow(
              row = doc.descriptionList.getElementsByClass("govuk-summary-list__row").get(0),
              keyText = testKey1,
              valueText = testValue1,
              actionText = testActionMessage1,
              actionHiddenText = testActionHiddenText1,
              actionHref = testActionUrl1
            )
          }

          "must generate correct content when there is no call to action" in {
            validateSummaryListRowNoAction(
              row = doc.descriptionList.getElementsByClass("govuk-summary-list__row").get(1),
              keyText = testKey2,
              valueText = testValue2
            )
          }

          "description list must have css class 'govuk-summary-list'" in {
            doc.descriptionList.hasClass("govuk-summary-list") mustBe true
          }
        }
      }

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithOrWithoutError(hasError = false)

      doc.createTestsWithLargeCaption(
        pageCaption
      )

      doc.createTestsWithSubmissionButton(
        action = certificateRoutes.CertificateCheckYourAnswersController.onSubmit(),
        buttonText = pageButtonText
      )
    }
  }

  def validateSummaryListRow(
      row: Element,
      keyText: String,
      valueText: String,
      actionText: String,
      actionHiddenText: String,
      actionHref: String
  ): Unit = {
    val key = row.select("dt.govuk-summary-list__key")
    key.size() mustBe 1
    withClue("row keyText mismatch:\n") {
      key.get(0).text() mustBe keyText
    }

    val value = row.select("dd.govuk-summary-list__value")
    value.size() mustBe 1
    withClue("row valueText mismatch:\n") {
      value.get(0).text() mustBe valueText
    }

    val action = row.select("dd.govuk-summary-list__actions")
    withClue("row action not found!:\n") {
      action.size() mustBe 1
    }

    val linkText = action.get(0).select("a")
    linkText.size() mustBe 1
    withClue("row actionHref mismatch:\n") {
      linkText.get(0).attr("href") mustBe actionHref
    }
    withClue("row actionHiddenText mismatch:\n") {
      linkText.get(0).select("span.govuk-visually-hidden").text() mustBe actionHiddenText
    }
    linkText.get(0).select("span.govuk-visually-hidden").remove()
    withClue("row actionText mismatch:\n") {
      linkText.get(0).text() mustBe actionText
    }
  }

  def validateSummaryListRowNoAction(
      row: Element,
      keyText: String,
      valueText: String
  ): Unit = {
    val key = row.select("dt.govuk-summary-list__key")
    key.size() mustBe 1
    withClue("row keyText mismatch:\n") {
      key.get(0).text() mustBe keyText
    }

    val value = row.select("dd.govuk-summary-list__value")
    value.size() mustBe 1
    withClue("row valueText mismatch:\n") {
      value.get(0).text() mustBe valueText
    }

    val action = row.select("dd.govuk-summary-list__actions")
    withClue("row action found!:\n") {
      action.size() mustBe 0
    }
  }

  extension (target: Document | Element) {
    def summaryListGrid: Elements    = target.select("div.govuk-grid-row")
    def summaryListContent: Elements = summaryListGrid.select("div.govuk-grid-column-two-thirds")
    def descriptionLists: Elements   = summaryListContent.select("dl")
    def descriptionList: Element     = descriptionLists.get(0)
  }
}

object CertificateCheckYourAnswersViewSpec {
  val pageHeading           = "Check your answers"
  val pageTitle             = "Check your answers"
  val pageCaption           = "Submit a certificate"
  val pageButtonText        = "Confirm and submit"
  val testKey1              = "testKey"
  val testValue1            = "nonEmptyString"
  val testActionMessage1    = "dummyMessage"
  val testActionUrl1        = "dummyUrl"
  val testActionHiddenText1 = "dummyVisuallyHiddenText"
  val testKey2              = "testKey2"
  val testValue2            = "nonEmptyString2"
}

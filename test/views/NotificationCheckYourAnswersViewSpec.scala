/*
 * Copyright 2025 HM Revenue & Customs
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
import models.CheckMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.NotificationCheckYourAnswersViewSpec.*
import views.html.NotificationCheckYourAnswersView
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, Value as SLValue}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.converters.*

class NotificationCheckYourAnswersViewSpec extends ViewSpecBase[NotificationCheckYourAnswersView] {

  private def generateView(summaryList: SummaryList): Document = Jsoup.parse(SUT(summaryList).toString)

  "NotificationCheckYourAnswersView" - {

    "empty summary list must result in no table rows" - {
      val summaryList                = SummaryList()
      val doc: Document              = generateView(summaryList)
      def descriptionLists: Elements = doc.getMainContent.getElementsByTag("dl")

      "page must have description list" in {
        descriptionLists.size() mustBe 1
      }

      "description list must have no rows" in {
        val descriptionList = descriptionLists.get(0)
        descriptionList.getElementsByClass("govuk-summary-list__row").size() mustBe 0
      }

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithOrWithoutError(hasError = false)

      doc.createTestsWithCaption(
        pageCaption
      )

      doc.createTestsWithSubmissionButton(
        action = routes.NotificationCheckYourAnswersController.onSubmit(),
        buttonText = pageButtonText
      )
    }

    "non-empty summary list must result in a table with rows" - {
      val summaryList =
        SummaryList(rows =
          Seq(
            SummaryListRowViewModel(
              key = "testKey".toKey,
              value = SLValue("nonEmptyString".toText),
              actions = Seq(
                ActionItemViewModel(
                  "dummyMessage".toText,
                  "dummyUrl"
                )
                  .withVisuallyHiddenText("dummyVisuallyHiddenText")
              )
            ),
            SummaryListRowViewModel(
              key = "testKey2".toKey,
              value = SLValue("nonEmptyString2".toText)
            )
          )
        )
      val doc: Document              = generateView(summaryList)
      def descriptionLists: Elements = doc.getMainContent.getElementsByTag("dl")

      "page must have description list" in {
        descriptionLists.size() mustBe 1
      }

      "description list must have two rows" in {
        val descriptionList = descriptionLists.get(0)
        descriptionList.getElementsByClass("govuk-summary-list__row").size() mustBe 2
      }

      "description list value must be 'nonEmptyString'" in {
        val descriptionList = descriptionLists.get(0)
        validateSummaryListRow(
          row = descriptionList.getElementsByClass("govuk-summary-list__row").get(0),
          keyText = "testKey",
          valueText = "nonEmptyString",
          actionText = "dummyMessage",
          actionHiddenText = "dummyVisuallyHiddenText",
          actionHref = "dummyUrl"
        )
        validateSummaryListRowNoAction(
          row = descriptionList.getElementsByClass("govuk-summary-list__row").get(1),
          keyText = "testKey2",
          valueText = "nonEmptyString2"
        )
      }

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithOrWithoutError(hasError = false)

      doc.createTestsWithCaption(
        pageCaption
      )

      doc.createTestsWithSubmissionButton(
        action = routes.NotificationCheckYourAnswersController.onSubmit(),
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
}

object NotificationCheckYourAnswersViewSpec {
  val pageHeading    = "Check your answers"
  val pageTitle      = "Submit a notification"
  val pageCaption    = "Submit a notification"
  val pageButtonText = "Continue"
}

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

package viewmodels.checkAnswers.notification

import base.SpecBase
import controllers.notification.routes as notificationRoutes
import models.CheckMode
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.notification.*
import play.api.i18n.{Messages, MessagesApi}
import uk.gov.hmrc.govukfrontend.views.Implicits.RichString

import java.time.LocalDate
import viewmodels.checkAnswers.notification.NotificationMultiSaoPreviousOfficerEndDateSummarySpec.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class NotificationMultiSaoPreviousOfficerEndDateSummarySpec extends SpecBase with GuiceOneAppPerSuite {
  given Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  "NotificationMultiSaoPreviousOfficerEndDateSummary.row" - {

    "when there is no answer for NotificationMultiSaoPreviousOfficerEndDatePage" - {
      "must return None" in {
        def SUT = NotificationMultiSaoPreviousOfficerEndDateSummary.row(emptyUserAnswers, 0)

        SUT mustBe None
      }
    }

    "when there is a user answer for NotificationMultiSaoPreviousOfficerEndDatePage" - {

      "when end date is part of a complete chain of Sao answers" - {

        val exampleDate = LocalDate.of(2000, 1, 1)

        val userAnswersWithTwoSaos = emptyUserAnswers
          .set(NotificationMultiSaoPreviousOfficerNamePage(0), "Firstname Lastname")
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(0), LocalDate.now)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(0), LocalDate.now)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(0), false)
          .get
          .set(NotificationMultiSaoPreviousOfficerNamePage(1), "Firstname Lastname")
          .get
          .set(NotificationMultiSaoPreviousOfficerStartDatePage(1), LocalDate.now)
          .get
          .set(NotificationMultiSaoPreviousOfficerEndDatePage(1), exampleDate)
          .get
          .set(NotificationMultiSaoAreAllAddedPage(1), true)
          .get

        val sut = NotificationMultiSaoPreviousOfficerEndDateSummary
          .row(userAnswersWithTwoSaos, 1)
          .get

        "must have expected key" in {
          sut.key mustBe keyText.toKey
        }

        "must have expected value" - {
          "must show '1 January 2000' when user answers is 1st Jan 2000" in {
            sut.value.content mustBe HtmlContent(
              s"""<span data-test-id="previous-sao-end-date-2">$expectedDate</span>"""
            )
          }
        }

        "must have expected action" - {
          def actions = sut.actions

          "must only have one action" in {
            withClue("must be 1 action\n") {
              actions.size mustBe 1
            }
            withClue("must be 1 item in the action\n") {
              actions.head.items.size mustBe 1
            }
          }

          def action = actions.head.items.head

          "must have expected text" in {
            action.content mustBe "Change".toText
          }

          "must have expected url" in {
            action.href mustBe notificationRoutes.NotificationMultiSaoPreviousOfficerEndDateController
              .onPageLoad(CheckMode, 1)
              .url
          }

          "must have expected hidden text" in {
            action.visuallyHiddenText.get mustBe "NotificationMultiSaoPreviousOfficerEndDate"
          }
        }
      }
      "when end date is not part of a complete chain of Sao answers" - {
        val sut = NotificationMultiSaoPreviousOfficerEndDateSummary
          .row(emptyUserAnswers, 0)

        "None is returned" in {
          sut mustBe None
        }
      }
    }
  }
}

object NotificationMultiSaoPreviousOfficerEndDateSummarySpec {
  val keyText      = "End date"
  val expectedDate = "1 January 2000"
}

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

package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.NotificationPreviousSaoNamePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.converters.*
import viewmodels.govuk.summarylist.*

object NotificationPreviousSaoNameSummary {

  def row(answers: UserAnswers, index: Int)(using messages: Messages): Option[SummaryListRow] =
    answers.get(NotificationPreviousSaoNamePage(index)).map { answer =>
      SummaryListRowViewModel(
        key = messages("notificationPreviousSaoName.checkYourAnswersLabel").toKey,
        value = ValueViewModel(answer.toText),
        actions = Seq(
          ActionItemViewModel(
            messages("site.change").toText,
            routes.NotificationPreviousSaoNameController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("notificationPreviousSaoName.change.hidden"))
        )
      )
    }
}

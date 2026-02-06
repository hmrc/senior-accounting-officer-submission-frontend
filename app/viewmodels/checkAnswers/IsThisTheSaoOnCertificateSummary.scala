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

package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.IsThisTheSaoOnCertificatePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.converters.*
import viewmodels.govuk.summarylist.*

object IsThisTheSaoOnCertificateSummary {

  def row(answers: UserAnswers)(using messages: Messages): Option[SummaryListRow] =
    answers.get(IsThisTheSaoOnCertificatePage).map { answer =>

      val value = if answer then "site.yes" else "site.no"

      SummaryListRowViewModel(
        key = messages("isThisTheSaoOnCertificate.checkYourAnswersLabel").toKey,
        value = ValueViewModel(messages(value).toText),
        actions = Seq(
          ActionItemViewModel(
            messages("site.change").toText,
            routes.IsThisTheSaoOnCertificateController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("isThisTheSaoOnCertificate.change.hidden"))
            .withAttribute("data-test-id", "change-is-this-the-sao-link")
        )
      )
    }
}

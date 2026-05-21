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
import pages.CombinedCertificateDeclarationSaoPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.converters.*
import viewmodels.govuk.summarylist.*

object CombinedCertificateDeclarationSaoSummary {

  def row(answers: UserAnswers)(using messages: Messages): Option[SummaryListRow] =
    answers.get(CombinedCertificateDeclarationSaoPage).map { answer =>
      val value = HtmlFormat.escape(answer.sao).toString + "<br/>" + HtmlFormat.escape(answer.proxy).toString

      SummaryListRowViewModel(
        key = messages("combinedCertificateDeclarationSao.checkYourAnswersLabel").toKey,
        value = ValueViewModel(HtmlContent(value)),
        actions = Seq(
          ActionItemViewModel(
            messages("site.change").toText,
            routes.CombinedCertificateDeclarationSaoController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("combinedCertificateDeclarationSao.change.hidden"))
        )
      )
    }
}

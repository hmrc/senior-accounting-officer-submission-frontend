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

package services

import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.checkAnswers.{SaoEmailCommunicationChoiceSummary, SaoEmailSummary, SaoNameSummary}

class CertificateCheckYourAnswersService {
  // TODO - [DN - AG - RS] 30/01/26 : SaoEmailSummary, SaoEmailCommunicationChoiceSummary both are returning options, Is this correct ?
  def getSummaryList(userAnswers: UserAnswers)(using Messages): SummaryList = {
    SummaryList(rows =
      Seq(
        SaoNameSummary
          .row(userAnswers)
          .getOrElse(SummaryListRow.apply(Key(), Value(), "", None)), // TODO remove this when above is discussed
        SaoEmailSummary.row(userAnswers).getOrElse(SummaryListRow.apply(Key(), Value(), "", None)),
        SaoEmailCommunicationChoiceSummary.row(userAnswers).getOrElse(SummaryListRow.apply(Key(), Value(), "", None))
      )
    )
  }
}

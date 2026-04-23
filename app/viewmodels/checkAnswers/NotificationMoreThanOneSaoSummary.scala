package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.NotificationMoreThanOneSaoPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.converters.*

object NotificationMoreThanOneSaoSummary  {

  def row(answers: UserAnswers)(using messages: Messages): Option[SummaryListRow] =
    answers.get(NotificationMoreThanOneSaoPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = messages("notificationMoreThanOneSao.checkYourAnswersLabel").toKey,
          value   = ValueViewModel(messages(value).toText),
          actions = Seq(
            ActionItemViewModel(messages("site.change").toText, routes.NotificationMoreThanOneSaoController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("notificationMoreThanOneSao.change.hidden"))
          )
        )
    }
}

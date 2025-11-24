package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.$className$Page
import play.api.i18n.{Lang, Messages}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.govuk.summarylist.*
import viewmodels.converters.*

object $className$Summary  {

  def row(answers: UserAnswers)(using messages: Messages): Option[SummaryListRow] =
    answers.get($className$Page).map {
      answer =>

        given Lang = messages.lang

        SummaryListRowViewModel(
          key     = messages("$className;format="decap"$.checkYourAnswersLabel").toKey,
          value   = ValueViewModel(answer.format(dateTimeFormat()).toText),
          actions = Seq(
            ActionItemViewModel(messages("site.change").toText, routes.$className$Controller.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("$className;format="decap"$.change.hidden"))
          )
        )
    }
}

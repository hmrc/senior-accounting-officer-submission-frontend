package viewmodels.checkAnswers

import base.SpecBase
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import uk.gov.hmrc.govukfrontend.views.Implicits.RichString

class NotificationAdditionalInformationSummarySpec extends SpecBase with GuiceOneAppPerSuite {
  given Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)
  val testDate = "{0}"
  "" - {
    "Hello Test" in {
      val SUT = NotificationAdditionalInformationSummary.row(emptyUserAnswers)
      SUT.key mustBe "Additional information".toKey
      SUT.actions.get.items.head.visuallyHiddenText.get mustBe s"the additional information supplied for the notification (Financial year end $testDate)"
    }
  }
}

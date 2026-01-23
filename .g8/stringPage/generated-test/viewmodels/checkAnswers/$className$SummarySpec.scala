package viewmodels.checkAnswers

import base.SpecBase
import controllers.routes
import models.CheckMode
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.$className$Page
import play.api.i18n.{Messages, MessagesApi}
import uk.gov.hmrc.govukfrontend.views.Implicits.RichString

class $className$SummarySpec extends SpecBase with GuiceOneAppPerSuite {
  given Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  "$className$Summary.row" - {

    "when there is no answer for $className$Page" - {
      "must return None" in {
        def SUT = $className$Summary.row(emptyUserAnswers)

        SUT mustBe None
      }
    }

    "when there is a user answer for $className$Page" - {
      def testUserAnswers(answer: String) =
        emptyUserAnswers.set($className$Page, answer).get

      def SUT(answer: String = "") = $className$Summary.row(testUserAnswers(answer)).get

      "must have expected key" in {
        SUT().key mustBe "$className;format="decap"$".toKey
      }

      "expected value" - {
        "must show 'test$className$' when user answers is 'test$className$'" in {
          SUT(answer = "test$className$").value.content mustBe "test$className$".toText
        }
      }

      "expected action" - {
        def actions = SUT().actions

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
          action.href mustBe routes.$className$Controller
            .onPageLoad(CheckMode)
            .url
        }

        "must have expected hidden text" in {
          action.visuallyHiddenText.get mustBe "$className$"
        }
      }
    }
  }

}

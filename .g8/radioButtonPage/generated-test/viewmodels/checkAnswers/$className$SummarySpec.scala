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

import base.SpecBase
import controllers.routes
import models.{CheckMode, $className$}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.$className$Page
import play.api.i18n.{Messages, MessagesApi}
import uk.gov.hmrc.govukfrontend.views.Implicits.RichString
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

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
      def testUserAnswers(answer: $className$) =
        emptyUserAnswers.set($className$Page, answer).get

      def SUT(answer: $className$ = $className$.$option1key;format="Camel"$) = $className$Summary.row(testUserAnswers(answer)).get

      "must have expected key" in {
        SUT().key mustBe "$className;format="decap"$".toKey
      }

      "expected value" - {
        "must show '$option1msg;format="Camel"$' when user answers is $option1key$" in {
          SUT(answer = $className$.$option1key;format="Camel"$).value.content mustBe HtmlContent("$option1msg$")
        }

        "must show '$option2msg;format="Camel"$' when user answers is $option2key$" in {
          SUT(answer = $className$.$option2key;format="Camel"$).value.content mustBe HtmlContent("$option2msg$")
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

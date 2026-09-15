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

package viewmodels.checkAnswers.certificate

import base.SpecBase
import controllers.certificate.routes as certificateRoutes
import models.CheckMode
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.certificate.CertificateDeclarationStandInPage
import play.api.i18n.{Messages, MessagesApi}
import uk.gov.hmrc.govukfrontend.views.Implicits.RichString
import CertificateDeclarationStandInSummarySpec.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import models.certificate.CertificateDeclarationStandIn

class CertificateDeclarationStandInSummarySpec extends SpecBase with GuiceOneAppPerSuite {
  given Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  "CertificateDeclarationStandInSummary.standInRow" - {

    "when there is no answer for CertificateDeclarationStandInPage" - {
      "must return None" in {
        def SUT = CertificateDeclarationStandInSummary.standInRow(emptyUserAnswers)

        SUT mustBe None
      }
    }

    "when there is a user answer for CertificateDeclarationStandInPage" - {
      def testUserAnswers(answer: CertificateDeclarationStandIn) =
        emptyUserAnswers.set(CertificateDeclarationStandInPage, answer).get

      def SUT(answer: CertificateDeclarationStandIn) =
        CertificateDeclarationStandInSummary.standInRow(testUserAnswers(answer)).get

      "must have expected key" in {
        SUT(expectedValue).key mustBe expectedStandInKey.toKey
      }

      "expected value" in {
        SUT(expectedValue).value.content mustBe HtmlContent(
          s"""<span data-test-id="$expectedStandInValueId">$standInName</span>"""
        )
      }

      "expected action" - {
        def actions = SUT(expectedValue).actions

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
          action.href mustBe certificateRoutes.CertificateDeclarationStandInController
            .onPageLoad(CheckMode)
            .url
        }

        "must have expected hidden text" in {
          action.visuallyHiddenText.get mustBe "StandInName"
        }
      }
    }
  }

  "CertificateDeclarationStandInSummary.saoRow" - {

    "when there is no answer for CertificateDeclarationStandInPage" - {
      "must return None" in {
        def SUT = CertificateDeclarationStandInSummary.saoRow(emptyUserAnswers)

        SUT mustBe None
      }
    }

    "when there is a user answer for CertificateDeclarationStandInPage" - {
      def testUserAnswers(answer: CertificateDeclarationStandIn) =
        emptyUserAnswers.set(CertificateDeclarationStandInPage, answer).get

      def SUT(answer: CertificateDeclarationStandIn) =
        CertificateDeclarationStandInSummary.saoRow(testUserAnswers(answer)).get

      "must have expected key" in {
        SUT(expectedValue).key mustBe expectedSaoKey.toKey
      }

      "expected value" in {
        SUT(expectedValue).value.content mustBe HtmlContent(
          s"""<span data-test-id="$expectedSaoValueId">$saoName</span>"""
        )
      }

      "expected action" - {
        def actions = SUT(expectedValue).actions

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
          action.href mustBe certificateRoutes.CertificateDeclarationStandInController
            .onPageLoad(CheckMode)
            .url
        }

        "must have expected hidden text" in {
          action.visuallyHiddenText.get mustBe "SaoName"
        }
      }
    }
  }
}

object CertificateDeclarationStandInSummarySpec {
  val expectedSaoKey         = "SAO name on the declaration"
  val expectedStandInKey     = "Authorised name on the declaration"
  val standInName            = "Firstname Lastname"
  val saoName                = "Firstname Lastname II"
  val expectedStandInValueId = "declaration-stand-in-value"
  val expectedSaoValueId     = "declaration-sao-value"
  val expectedValue          = CertificateDeclarationStandIn(standInName, saoName)
}

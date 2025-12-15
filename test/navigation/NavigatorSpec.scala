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

package navigation

import base.SpecBase
import controllers.routes
import models.{CheckMode, NormalMode, UserAnswers}
import pages.{NotificationGuidancePage, Page}

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator.nextPage" - {

    "in Normal mode" - {

      "must throw an not-implemented error for an unspecified configuration" in {
        case object UnknownPage extends Page
        intercept[NotImplementedError] {
          navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id"))
        }
      }

      "when on NotificationGuidancePage, must go to notification additional information page" in {
        navigator.nextPage(
          NotificationGuidancePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.NotificationAdditionalInformationController.onPageLoad(NormalMode)
      }
    }

    "in Check mode" - {

      "must throw an not-implemented error for an unspecified configuration" in {
        case object UnknownPage extends Page
        intercept[NotImplementedError] {
          navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id"))
        }
      }

    }
  }
}

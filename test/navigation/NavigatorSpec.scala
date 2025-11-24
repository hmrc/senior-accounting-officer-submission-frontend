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
import models.ContactType.*
import models.*
import pages.*

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "when the user is in the add first contact details journey" - {
        "must go from contact name to contact email" in {
          navigator.nextPage(ContactNamePage(First), NormalMode, UserAnswers("id")) mustBe routes.ContactEmailController
            .onPageLoad(First, NormalMode)
        }

        "must go from contact email to add another" in {
          navigator.nextPage(
            ContactEmailPage(First),
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.ContactHaveYouAddedAllController
            .onPageLoad(First)
        }

        "on add another page" - {
          "when the user answers Yes must go to contact check your answers" in {
            navigator.nextPage(
              ContactHaveYouAddedAllPage(First),
              NormalMode,
              UserAnswers("id").set(ContactHaveYouAddedAllPage(First), ContactHaveYouAddedAll.Yes).get
            ) mustBe routes.ContactCheckYourAnswersController.onPageLoad()
          }
          "when the user answers No must go to 2nd contact name" in {
            navigator.nextPage(
              ContactHaveYouAddedAllPage(First),
              NormalMode,
              UserAnswers("id").set(ContactHaveYouAddedAllPage(First), ContactHaveYouAddedAll.No).get
            ) mustBe routes.ContactNameController
              .onPageLoad(Second, NormalMode)
          }
        }
      }

      "when the user is in the add second contact details journey" - {
        "must go from contact name to contact email" in {
          navigator.nextPage(
            ContactNamePage(Second),
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.ContactEmailController
            .onPageLoad(Second, NormalMode)
        }

        "must go from contact email to review page" in {
          navigator.nextPage(
            ContactEmailPage(Second),
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.ContactCheckYourAnswersController
            .onPageLoad()
        }
      }
    }

    "in Check mode" - {

      "must go from a non-existant page in the edit route map to ContactCheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.ContactCheckYourAnswersController
          .onPageLoad()
      }
    }
  }
}

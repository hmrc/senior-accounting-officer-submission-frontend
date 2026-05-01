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

package forms

import java.time.{LocalDate, ZoneOffset}
import forms.behaviours.DateBehaviours
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class MoreSaoSubmitNotificationFirstStartDateFormProviderSpec extends DateBehaviours {

  private given messages2: Messages = stubMessages()
  val form                          = new MoreSaoSubmitNotificationFirstStartDateFormProvider()()

  val requiredAllKey = "moreSaoSubmitNotificationFirstStartDate.error.required.all"
  val requiredTwoKey = "moreSaoSubmitNotificationFirstStartDate.error.required.two"
  val requiredKey    = "moreSaoSubmitNotificationFirstStartDate.error.required"
  val invalidKey     = "moreSaoSubmitNotificationFirstStartDate.error.invalid"

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", requiredAllKey)
  }

  "error message keys must map to the expected text" - {
    createTestWithErrorMessageAssertion(
      key = requiredAllKey,
      message = "The date must be a real date"
    )

    createTestWithErrorMessageAssertion(
      key = requiredTwoKey,
      message = "The date must include {0} and {1}"
    )

    createTestWithErrorMessageAssertion(
      key = requiredKey,
      message = "The date must include {0}"
    )

    createTestWithErrorMessageAssertion(
      key = invalidKey,
      message = "Enter the date in the correct format"
    )
  }
}

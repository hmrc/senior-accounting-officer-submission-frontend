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

import forms.behaviours.DateBehaviours
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.{LocalDate, ZoneOffset}

class NotificationMoreSaoSecondStartDateFormProviderSpec extends DateBehaviours {

  private given messages2: Messages = stubMessages()
  val form                          = new NotificationMoreSaoSecondStartDateFormProvider()()

  // LDS ignore
  val requiredAllKey = "notificationMoreSaoSecondStartDate.error.required.all"
  // LDS ignore
  val requiredTwoKey = "notificationMoreSaoSecondStartDate.error.required.two"
  // LDS ignore
  val requiredKey = "notificationMoreSaoSecondStartDate.error.required"
  // LDS ignore
  val invalidKey = "notificationMoreSaoSecondStartDate.error.invalid"

  ".value input field" - {

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

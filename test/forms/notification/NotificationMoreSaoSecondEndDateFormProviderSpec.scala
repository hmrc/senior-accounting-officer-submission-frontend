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

package forms.notification

import forms.behaviours.DateBehaviours
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.{LocalDate, ZoneOffset}

class NotificationMoreSaoSecondEndDateFormProviderSpec extends DateBehaviours {

  private given messages2: Messages = stubMessages()
  val form                          = new NotificationMoreSaoSecondEndDateFormProvider()()

  val requiredAllKey = "notificationMoreSaoSecondEndDate.error.required.all"
  val requiredTwoKey = "notificationMoreSaoSecondEndDate.error.required.two"
  val requiredKey    = "notificationMoreSaoSecondEndDate.error.required"
  val invalidKey     = "notificationMoreSaoSecondEndDate.error.invalid"

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
      message = "Enter the end date the SAO was responsible for the group’s tax accounting arrangements"
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

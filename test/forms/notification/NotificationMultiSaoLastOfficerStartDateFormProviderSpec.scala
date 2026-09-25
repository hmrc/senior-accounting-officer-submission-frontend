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
import play.api.data.{Form, FormError}

import java.time.{Clock, LocalDate}

class NotificationMultiSaoLastOfficerStartDateFormProviderSpec extends DateBehaviours {

  val saoName               = "Firstname Lastname"
  val form: Form[LocalDate] =
    app.injector.instanceOf[NotificationMultiSaoLastOfficerStartDateFormProvider].apply(saoName)

  // LDS ignore
  val requiredAllKey = "notificationMultiSaoLastOfficerStartDate.error.required.all"
  // LDS ignore
  val requiredTwoKey = "notificationMultiSaoLastOfficerStartDate.error.required.two"
  // LDS ignore
  val requiredKey = "notificationMultiSaoLastOfficerStartDate.error.required"
  // LDS ignore
  val invalidKey = "notificationMultiSaoLastOfficerStartDate.error.invalid"
  // LDS ignore
  val notPastDateKey = "notificationMultiSaoLastOfficerStartDate.error.notPastDate"

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(1900, 1, 1),
      max = LocalDate.now(app.injector.instanceOf[Clock]).minusDays(1)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", requiredAllKey, errorArgs = Seq(saoName))

    behave like dateFieldWithMax(
      form,
      key = "value",
      max = LocalDate.now(app.injector.instanceOf[Clock]).minusDays(1),
      formError = FormError("value", notPastDateKey)
    )

    behave like dateFieldWithMin(
      form,
      key = "value",
      min = LocalDate.of(1900, 1, 1),
      formError = FormError("value", invalidKey)
    )
  }

  "error message keys must map to the expected text" - {
    createTestWithErrorMessageAssertion(
      key = requiredAllKey,
      message = s"Enter the date $saoName became the SAO",
      saoName
    )

    createTestWithErrorMessageAssertion(
      key = requiredTwoKey,
      message = "Enter the date {2} became the SAO"
    )

    createTestWithErrorMessageAssertion(
      key = requiredKey,
      message = "Enter the date {1} became the SAO"
    )

    createTestWithErrorMessageAssertion(
      key = invalidKey,
      message = "Start date of the SAO must be a real date"
    )

    createTestWithErrorMessageAssertion(
      key = notPastDateKey,
      message = "Start date of the SAO must be in the past"
    )
  }
}

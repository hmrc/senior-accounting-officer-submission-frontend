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

class NotificationMultiSaoPreviousOfficerStartDateFormProviderSpec extends DateBehaviours {

  val saoName               = "Firstname Lastname"
  val form: Form[LocalDate] =
    app.injector.instanceOf[NotificationMultiSaoPreviousOfficerStartDateFormProvider].apply(saoName)

  val requiredAllKey = "notificationMultiSaoPreviousOfficerStartDate.error.required.all"
  val requiredTwoKey = "notificationMultiSaoPreviousOfficerStartDate.error.required.two"
  val requiredKey    = "notificationMultiSaoPreviousOfficerStartDate.error.required"
  val invalidKey     = "notificationMultiSaoPreviousOfficerStartDate.error.invalid"
  val notPastDateKey = "notificationMultiSaoPreviousOfficerStartDate.error.notPastDate"

  ".value input field" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
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
  }

  "error message keys must map to the expected text" - {
    createTestWithErrorMessageAssertion(
      key = requiredAllKey,
      message = s"Enter the date $saoName became the SAO",
      args = saoName
    )

    createTestWithErrorMessageAssertion(
      key = requiredTwoKey,
      message = "The date must include a {0} and a {1}"
    )

    createTestWithErrorMessageAssertion(
      key = requiredKey,
      message = "The date must include a {0}"
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

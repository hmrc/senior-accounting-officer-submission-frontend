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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class CertificateSubmissionDeclarationFormProviderSpec extends StringFieldBehaviours {

  val form = new CertificateSubmissionDeclarationFormProvider()()

  val field1RequiredKey = "certificateSubmissionDeclaration.error.sao.required"
  val field1LengthKey = "certificateSubmissionDeclaration.error.sao.length"

  val field2RequiredKey = "certificateSubmissionDeclaration.error.proxy.required"
  val field2LengthKey = "certificateSubmissionDeclaration.error.proxy.length"

  ".sao" - {

    val fieldName = "sao"
    val maxLength = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, field1LengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, field1RequiredKey)
    )
  }

  ".proxy" - {

    val fieldName = "proxy"
    val maxLength = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, field2LengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, field2RequiredKey)
    )
  }

  "error message keys must map to the expected text" - {
    createTestWithErrorMessageAssertion(
      key = field1RequiredKey,
      message = "Enter sao"
    )

    createTestWithErrorMessageAssertion(
      key = field1LengthKey,
      message = "sao must be 100 characters or less"
    )

    createTestWithErrorMessageAssertion(
      key = field2RequiredKey,
      message = "Enter proxy"
    )

    createTestWithErrorMessageAssertion(
      key = field2LengthKey,
      message = "proxy must be 100 characters or less"
    )
  }
}

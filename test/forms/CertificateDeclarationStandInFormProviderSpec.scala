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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class CertificateDeclarationStandInFormProviderSpec extends StringFieldBehaviours {

  val requiredKeyStandInName = "certificateDeclarationStandIn.error.StandInName.required"
  val lengthKeyStandInName   = "certificateDeclarationStandIn.error.StandInName.length"
  val requiredKeySaoName     = "certificateDeclarationStandIn.error.SaoName.required"
  val lengthKeySaoName       = "certificateDeclarationStandIn.error.SaoName.length"
  val maxLength              = 105

  val form = new CertificateDeclarationStandInFormProvider()()
  ".standInNameInputValue" - {

    val fieldName = "StandInName"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKeyStandInName, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKeyStandInName)
    )
  }

  ".saoNameInputValue" - {

    val fieldName = "SaoName"
    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKeySaoName, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKeySaoName)
    )
  }

  "error message keys must map to the expected text" - {
    createTestWithErrorMessageAssertion(
      key = requiredKeyStandInName,
      message = "Enter the name of the person authorised to sign the certificate"
    )

    createTestWithErrorMessageAssertion(
      key = lengthKeyStandInName,
      message = "The authorised person name you enter must be 105 characters or less"
    )
  }
}

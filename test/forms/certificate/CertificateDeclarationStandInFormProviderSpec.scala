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

package forms.certificate

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class CertificateDeclarationStandInFormProviderSpec extends StringFieldBehaviours {

  val requiredKeyStandInName = "certificateDeclarationStandIn.error.standInName.required"
  val lengthKeyStandInName   = "certificateDeclarationStandIn.error.standInName.length"
  val requiredKeyCharsStandInName = "certificateDeclarationStandIn.error.standInName.invalidChars"
  val requiredKeySaoName     = "certificateDeclarationStandIn.error.saoName.required"
  val lengthKeySaoName       = "certificateDeclarationStandIn.error.saoName.length"
  val requiredKeyCharsSaoName = "certificateDeclarationStandIn.error.saoName.invalidChars"
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

    behave like fieldThatBindsInvalidSymbols(
      form,
      fieldName,
      FormError(fieldName, ArraySeq(requiredKeyCharsStandInName))
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

    behave like fieldThatBindsInvalidSymbols(
      form,
      fieldName,
      FormError(fieldName, ArraySeq(requiredKeyCharsSaoName))
    )
    
  }

  "error message keys must map to the expected text" - {
    createTestWithErrorMessageAssertion(
      key = requiredKeyStandInName,
      message = "Enter your full name"
    )

    createTestWithErrorMessageAssertion(
      key = lengthKeyStandInName,
      message = "Your name must be 105 characters or less"
    )

    createTestWithErrorMessageAssertion(
      key = requiredKeyCharsStandInName,
      message = "Your name must not include <, > or \""
    )

    createTestWithErrorMessageAssertion(
      key = requiredKeySaoName,
      message = "Enter the name of the SAO who authorised you to submit the certificate"
    )

    createTestWithErrorMessageAssertion(
      key = lengthKeySaoName,
      message = "Name of the SAO must be 105 characters or less"
    )

    createTestWithErrorMessageAssertion(
      key = requiredKeyCharsSaoName,
      message = "Name of the SAO must not include <, > or \""
    )

  }
}

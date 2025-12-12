package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class SaoNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "saoName.error.required"
  val lengthKey = "saoName.error.length"
  val maxLength = 100

  val form = new SaoNameFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "error message keys must map to the expected text" - {
    createTestWithErrorMessageAssertion(
      key = requiredKey,
      message = "Enter saoName"
    )

    createTestWithErrorMessageAssertion(
      key = lengthKey,
      message = "SaoName must be 100 characters or less"
    )
  }
}

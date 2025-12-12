package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class SaoEmailFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "saoEmail.error.required"
  val lengthKey = "saoEmail.error.length"
  val maxLength = 100

  val form = new SaoEmailFormProvider()()

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
      message = "Enter saoEmail"
    )

    createTestWithErrorMessageAssertion(
      key = lengthKey,
      message = "SaoEmail must be 100 characters or less"
    )
  }
}

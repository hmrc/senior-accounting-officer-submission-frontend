package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class NotificationMoreThanOneSaoFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "notificationMoreThanOneSao.error.required"
  val invalidKey = "error.boolean"

  val form = new NotificationMoreThanOneSaoFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
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
      message = "Select yes if notificationMoreThanOneSao"
    )
  }
}

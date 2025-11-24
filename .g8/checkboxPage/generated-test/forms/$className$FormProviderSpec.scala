package forms

import forms.behaviours.CheckboxFieldBehaviours
import models.$className$
import play.api.data.FormError

class $className$FormProviderSpec extends CheckboxFieldBehaviours {

  val form = new $className$FormProvider()()
  val requiredKey = "$className;format="decap"$.error.required"

  ".value" - {

    val fieldName = "value"

    behave like checkboxField[$className$](
      form,
      fieldName,
      validValues  = $className$.values,
      invalidError = FormError(s"\$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )

    "error message keys must map to the expected text" - {
      createTestWithErrorMessageAssertion(
        key = requiredKey,
        message = "Select $className;format="decap"$"
      )
    }
  }
}

package forms

import java.time.{LocalDate, ZoneOffset}
import forms.behaviours.DateBehaviours
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class $className$FormProviderSpec extends DateBehaviours {

  private given messages2: Messages = stubMessages()
  val form = new $className$FormProvider()()

  val requiredAllKey = "$className;format="decap"$.error.required.all"
  val requiredTwoKey = "$className;format="decap"$.error.required.two"
  val requiredKey = "$className;format="decap"$.error.required"
  val invalidKey = "$className;format="decap"$.error.invalid"

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
      message = "Enter the $className;format="decap"$"
    )

    createTestWithErrorMessageAssertion(
      key = requiredTwoKey,
      message = "The $className;format="decap"$ must include {0} and {1}"
    )

    createTestWithErrorMessageAssertion(
      key = requiredKey,
      message = "The $className;format="decap"$ must include {0}"
    )

    createTestWithErrorMessageAssertion(
      key = invalidKey,
      message = "Enter a real $className$"
    )
  }
}

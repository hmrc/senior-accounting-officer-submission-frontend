package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends IntFieldBehaviours {

  val form = new $className$FormProvider()()

  val requiredKey = "$className;format="decap"$.error.required"
  val nonNumericKey = "$className;format="decap"$.error.nonNumeric"
  val wholeNumberKey = "$className;format="decap"$.error.wholeNumber"
  val outOfRangeKey = "$className;format="decap"$.error.outOfRange"

  ".value" - {

    val fieldName = "value"

    val minimum = $minimum$
    val maximum = $maximum$

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, nonNumericKey),
      wholeNumberError = FormError(fieldName, wholeNumberKey)
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, outOfRangeKey, Seq(minimum, maximum))
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
      message = "Enter your $className;format="decap"$"
    )

    createTestWithErrorMessageAssertion(
      key = nonNumericKey,
      message = "Enter your $className;format="decap"$ using numbers"
    )

    createTestWithErrorMessageAssertion(
      key = wholeNumberKey,
      message = "Enter your $className;format="decap"$ using whole numbers"
    )

    createTestWithErrorMessageAssertion(
      key = outOfRangeKey,
      message = "$className;format="decap"$ must be between {0} and {1}"
    )
  }
}

package forms

import config.CurrencyFormatter.currencyFormat
import forms.behaviours.CurrencyFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

import scala.math.BigDecimal.RoundingMode

class $className$FormProviderSpec extends CurrencyFieldBehaviours {

  val form = new $className$FormProvider()()
  val fieldName = "value"
  val requiredKey = "$className;format="decap"$.error.required"
  val nonNumericKey = "$className;format="decap"$.error.nonNumeric"
  val invalidNumericKey = "$className;format="decap"$.error.invalidNumeric"
  val aboveMaximumKey = "$className;format="decap"$.error.aboveMaximum"
  val belowMinimumKey = "$className;format="decap"$.error.belowMinimum"

  ".value" - {

    val minimum = $minimum$
    val maximum = $maximum$

    val validDataGenerator =
      Gen.choose[BigDecimal](minimum, maximum)
        .map(_.setScale(2, RoundingMode.HALF_UP))
        .map(_.toString)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like currencyField(
      form,
      fieldName,
      nonNumericError     = FormError(fieldName, "$className;format="decap"$.error.nonNumeric"),
      invalidNumericError = FormError(fieldName, "$className;format="decap"$.error.invalidNumeric")
    )

    behave like currencyFieldWithMaximum(
      form,
      fieldName,
      maximum,
      FormError(fieldName, "$className;format="decap"$.error.aboveMaximum", Seq(currencyFormat(maximum)))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "$className;format="decap"$.error.required")
    )
  }

  "error message keys must map to the expected text" - {
    createTestWithErrorMessageAssertion(
      key = requiredKey,
      message = "Enter your $className;format="decap"$"
    )

    createTestWithErrorMessageAssertion(
      key = nonNumericKey,
      message = "Enter your $className;format="decap"$ using numbers and a decimal point"
    )

    createTestWithErrorMessageAssertion(
      key = invalidNumericKey,
      message = "Enter your $className;format="decap"$ using up to two decimal places"
    )
    
    createTestWithErrorMessageAssertion(
      key = aboveMaximumKey,
      message = "$className$ must be {0} or less"
    )

    createTestWithErrorMessageAssertion(
      key = belowMinimumKey,
      message = "$className$ must be {0} or more"
    )
  }
}

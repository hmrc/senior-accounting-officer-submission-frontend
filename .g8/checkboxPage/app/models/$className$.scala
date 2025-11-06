package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox.*

enum $className$(override val toString: String) {
  case $option1key;format="Camel"$ extends $className$("$option1key;format="decap"$")
  case $option2key;format="Camel"$ extends $className$("$option2key;format="decap"$")
}

object $className$ extends Enumerable.Implicits[$className$] {

  override def members: Array[$className$] = $className$.values

  def checkboxItems(using messages: Messages): Seq[CheckboxItem] =
    values.map { value =>
        CheckboxItemViewModel(
          content = Text(messages(s"$className;format="decap"$.\${value.toString}")),
          fieldId = "value",
          index   = value.ordinal,
          value   = value.toString
        )
    }

}

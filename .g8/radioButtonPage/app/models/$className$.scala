package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

enum $className$(override val toString: String) {
  case $option1key;format="Camel"$ extends $className$("$option1key;format="decap"$")
  case $option2key;format="Camel"$ extends $className$("$option2key;format="decap"$")
}

object $className$ extends Enumerable.Implicits[$className$] {

  override def members: Array[$className$] = $className$.values

  def options(using messages: Messages): Seq[RadioItem] = values.map { value =>
    RadioItem(
      content = Text(messages(s"$className;format="decap"$.\${value.toString}")),
      value   = Some(value.toString),
      id      = Some(s"value_\${value.ordinal}")
    )
  }

}

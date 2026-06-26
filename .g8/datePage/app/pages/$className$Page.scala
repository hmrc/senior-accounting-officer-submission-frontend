package pages

import java.time.LocalDate

import play.api.libs.json.JsPath

case object $className$Page extends CertificateOnlyPage with QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "$className;format="decap"$"
}

/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms

import forms.mappings.*
import play.api.data.Forms.{mapping, of, optional}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}

import javax.inject.Inject

final case class NotificationAdditionalInformation(
    value: Option[String],
    continueButton: Option[String] = None,
    skipButton: Option[String] = None
)

class NotificationAdditionalInformationFormProvider @Inject() extends Mappings {

  val maxLength = 100

  def apply(): Form[NotificationAdditionalInformation] =
    Form(
      mapping(
        "value"          -> of(customFormatter),
        "continueButton" -> optional(text("notificationAdditionalInformation.error.required")),
        "skipButton"     -> optional(text("notificationAdditionalInformation.error.required"))
      )(NotificationAdditionalInformation.apply)((n) => Some(n.value, n.continueButton, n.skipButton))
    )

  private def customFormatter: Formatter[Option[String]] =
    new Formatter[Option[String]] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
        val isSkip = data.get("button") == Some("Skip")

        data.get(key) match {
          case _ if isSkip                                                              => Right(None)
          case Some(fieldValue) if fieldValue.nonEmpty && fieldValue.length < maxLength => Right(Some(fieldValue))
          case Some(fieldValue) if fieldValue.length >= maxLength                       =>
            Left(Seq(FormError(key, "notificationAdditionalInformation.error.length", Seq(maxLength))))
          case _ => Left(Seq(FormError(key, "notificationAdditionalInformation.error.required")))
        }
      }

      override def unbind(key: String, value: Option[String]): Map[String, String] =
        Map(key -> value.getOrElse(""))
    }

}

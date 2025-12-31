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
import models.NotificationAdditionalInformation
import play.api.data.Forms.{mapping, of, optional}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}

import javax.inject.Inject

class NotificationAdditionalInformationFormProvider @Inject() extends Mappings {

  val maxLength     = 100
  val requiredError = "notificationAdditionalInformation.error.required"
  val lengthError   = "notificationAdditionalInformation.error.length"

  val skipButtonField = "skipButton"

  def apply(): Form[NotificationAdditionalInformation] =
    Form(
      mapping(
        "value"          -> of(customFormatter),
        "continueButton" -> optional(text()),
        skipButtonField  -> optional(text())
      )(NotificationAdditionalInformation.apply)((n) => Some(n.value, n.continueButton, n.skipButton))
    )

  private def customFormatter: Formatter[Option[String]] =
    new Formatter[Option[String]] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
        val isSkip = data.contains(skipButtonField)

        val fieldValue = data
          .get(key)
          .map(_.trim)
          .filter(_.nonEmpty)

        if isSkip then {
          Right(None)
        } else {
          fieldValue match {
            case None                                              => Left(Seq(FormError(key, requiredError)))
            case Some(fieldValue) if fieldValue.length > maxLength =>
              Left(Seq(FormError(key, lengthError, Seq(maxLength))))
            case Some(fieldValue) => Right(Some(fieldValue))
          }
        }
      }

      override def unbind(key: String, value: Option[String]): Map[String, String] =
        Map(key -> value.getOrElse(""))
    }
}

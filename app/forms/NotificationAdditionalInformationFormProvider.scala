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
import play.api.data.Forms.of
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms, Mapping}

import javax.inject.Inject

class NotificationAdditionalInformationFormProvider @Inject() extends Mappings {

  val maxLength     = 5000
  val requiredError = "notificationAdditionalInformation.error.required"
  val lengthError   = "notificationAdditionalInformation.error.length"

  val skipButtonField = "skipButton"

  def apply(): Form[Option[String]] =
    Form(
      "value" -> mandatoryUnlessSkipped(
        text(errorKey = requiredError).verifying(maxLength(maxLength, lengthError))
      )
    )

  private def mandatoryUnlessSkipped(mapping: Mapping[String]): Mapping[Option[String]] =
    of(new Formatter[Option[String]] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
        val isSkip = data.contains(skipButtonField)

        if isSkip then {
          Right(None)
        } else {
          mapping.withPrefix(key).bind(data).map(Some.apply)
        }
      }

      override def unbind(key: String, value: Option[String]): Map[String, String] =
        value.fold(Map.empty) {
          case v if v.nonEmpty => Map(key -> v)
          case _               => Map.empty
        }
    })
}

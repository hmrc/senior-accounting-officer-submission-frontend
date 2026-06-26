/*
 * Copyright 2026 HM Revenue & Customs
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

package models.upscan

import pages.Page.{CERTIFICATE_PATH, NOTIFICATION_PATH}
import pages.certificate.CertificateUploadStatePage
import pages.notification.NotificationUploadStatePage
import play.api.mvc.QueryStringBindable
import queries.{Gettable, Settable}

enum UploadJourney(
    override val toString: String,
    val page: Gettable[FileUploadState] with Settable[FileUploadState],
    val uploadPath: String
) {
  case Notification
      extends UploadJourney(
        "notification",
        NotificationUploadStatePage,
        s"data.$NOTIFICATION_PATH.$NotificationUploadStatePage"
      )
  case Certificate
      extends UploadJourney(
        "certificate",
        CertificateUploadStatePage,
        s"data.$CERTIFICATE_PATH.$CertificateUploadStatePage"
      )
}

object UploadJourney {
  def fromString(value: String): Option[UploadJourney] =
    UploadJourney.values.find(_.toString == value)

  given QueryStringBindable[UploadJourney] with {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, UploadJourney]] =
      QueryStringBindable.bindableString.bind(key, params).map {
        _.flatMap(value => fromString(value).toRight(s"Invalid upload journey: $value"))
      }

    override def unbind(key: String, value: UploadJourney): String =
      QueryStringBindable.bindableString.unbind(key, value.toString)
  }
}

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

package models

import play.api.mvc.QueryStringBindable

enum JourneySection {
  case Notification
  case Certificate
}

object JourneySection {

  given QueryStringBindable[JourneySection] = new QueryStringBindable[JourneySection] {

    private val stringBindable = summon[QueryStringBindable[String]]

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, JourneySection]] =
      stringBindable.bind(key, params).map {
        case Right(value) =>
          JourneySection.values
            .find(_.toString == value)
            .toRight(s"Unknown JourneySection: $value")
        case Left(error) => Left(error)
      }

    override def unbind(key: String, value: JourneySection): String =
      stringBindable.unbind(key, value.toString)
  }
}

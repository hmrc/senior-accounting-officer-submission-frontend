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

package models

import play.api.mvc.JavascriptLiteral
import play.api.mvc.QueryStringBindable

sealed trait Mode

case object CheckMode       extends Mode
case object NormalMode      extends Mode
case object TransactionMode extends Mode // TODO: remove when we start using a list pattern for multiple sao CYA

object Mode {

  given queryStringBinder(using stringBinder: QueryStringBindable[String]): QueryStringBindable[Mode] =
    new QueryStringBindable[Mode] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Mode]] = {
        for {
          mode <- stringBinder.bind(key, params)
        } yield {
          mode match {
            case Right(mode) =>
              mode match {
                case "NormalMode"      => Right(NormalMode)
                case "CheckMode"       => Right(CheckMode)
                case "TransactionMode" => Right(TransactionMode)
                case _                 => Left(UnbindableMode)
              }
            case _ => Left(UnbindableMode)
          }
        }
      }

      override def unbind(key: String, mode: Mode): String = {
        mode match {
          case NormalMode      => stringBinder.unbind(key, "NormalMode")
          case CheckMode       => stringBinder.unbind(key, "CheckMode")
          case TransactionMode => stringBinder.unbind(key, "TransactionMode")
        }
      }
    }

  given jsLiteral: JavascriptLiteral[Mode] = new JavascriptLiteral[Mode] {
    override def to(value: Mode): String = value match {
      case NormalMode      => "NormalMode"
      case CheckMode       => "CheckMode"
      case TransactionMode => "TransactionMode"
    }
  }

  val values: List[Mode] = List(NormalMode, CheckMode)

  private val UnbindableMode: String = "Unable to bind a Mode"
}

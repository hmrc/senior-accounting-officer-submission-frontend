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

import play.api.mvc.JavascriptLiteral

enum Area {
  case Committed, Transaction
}

object Area {
  val COMMITTED_PATH   = "Committed"
  val TRANSACTION_PATH = "Transaction"

  given jsLiteral: JavascriptLiteral[Area] = new JavascriptLiteral[Area] {
    override def to(value: Area): String = value match {
      case Area.Committed   => "committed"
      case Area.Transaction => "transaction"
    }
  }

  extension (mode: Mode) {
    def toArea: Area = {
      mode match {
        case NormalMode      => Area.Committed
        case CheckMode       => Area.Committed
        case TransactionMode => Area.Transaction
      }
    }
  }
}

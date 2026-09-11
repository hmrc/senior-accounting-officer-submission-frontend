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

enum Realm {
  case Actual, Shadow
}

object Realm {
  given jsLiteral: JavascriptLiteral[Realm] = new JavascriptLiteral[Realm] {
    override def to(value: Realm): String = value match {
      case Realm.Actual => "actual"
      case Realm.Shadow => "shadow"
    }
  }

  extension (mode: Mode) {
    def toRealm: Realm = {
      mode match {
        case NormalMode      => Realm.Actual
        case CheckMode       => Realm.Actual
        case TransactionMode => Realm.Shadow
      }
    }
  }
}

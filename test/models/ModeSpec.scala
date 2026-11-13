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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}

class ModeSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  "Mode query string binder should bind" - {

    "Normal Mode" in {
      val queryParameters: Map[String, Seq[String]] = Map("mode" -> Seq("NormalMode"))
      val result                                    = Mode.queryStringBinder.bind("mode", queryParameters)
      result mustBe Some(Right(NormalMode))

    }

    "Check Mode" in {

      val queryParameters: Map[String, Seq[String]] = Map("mode" -> Seq("CheckMode"))
      val result                                    = Mode.queryStringBinder.bind("mode", queryParameters)
      result mustBe Some(Right(CheckMode))

    }

    "Transaction Mode" in {

      val queryParameters: Map[String, Seq[String]] = Map("mode" -> Seq("TransactionMode"))
      val result                                    = Mode.queryStringBinder.bind("mode", queryParameters)
      result mustBe Some(Right(TransactionMode))

    }

    "invalid parameter" in {
      val modeParams: Map[String, Seq[String]] = Map("" -> Seq(""))
      val error                                = intercept[IllegalArgumentException] {
        Mode.queryStringBinder.bind("", modeParams)
      }
      error.getMessage mustBe "Unable to bind a Mode"
    }
  }

  "Mode must return the correct query string for" - {

    "NormalMode" in {

      val result = Mode.queryStringBinder.unbind("", NormalMode)
      result mustBe "=NormalMode"
    }

    "CheckMode" in {

      val result = Mode.queryStringBinder.unbind("", CheckMode)
      result mustBe "=CheckMode"
    }

    "TransactionMode" in {

      val result = Mode.queryStringBinder.unbind("", TransactionMode)
      result mustBe "=TransactionMode"
    }

  }
}

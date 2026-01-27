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

package forms.behaviours

import forms.FormSpec
import generators.Generators
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}

trait FieldBehaviours extends FormSpec with ScalaCheckPropertyChecks with Generators {

  def fieldThatBindsValidData(form: Form[?], fieldName: String, validDataGenerator: Gen[String]): Unit = {

    "must bind valid data" in {

      forAll(validDataGenerator -> "validDataItem") { (dataItem: String) =>
        val result = form.bind(Map(fieldName -> dataItem)).apply(fieldName)
        result.value.value mustBe dataItem
        result.errors mustBe empty
      }
    }
  }

  def mandatoryField(form: Form[?], fieldName: String, requiredError: FormError): Unit = {

    "must not bind when key is not present at all" in {

      val result = form.bind(emptyForm).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "must not bind empty string" in {

      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "must not bind whitespace" in {

      val result = form.bind(Map(fieldName -> "  	   ")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }
  }

  def fieldWithMaxEmailLength(
                               form: Form[?],
                               fieldName: String,
                               generator: Gen[String],
                               requiredError: FormError
                             ): Unit = {
    "must not bind email with invalid length" in {
      forAll(generator -> "longEmail") { (longEmailStr: String) =>
        whenever(longEmailStr.length > maxEmailLength) {
          val result = form.bind(Map(fieldName -> longEmailStr)).apply(fieldName)
          result.errors.toList must contain(requiredError)
        }
      }
    }
  }

  def fieldWithInvalidEmailformat(
                                   form: Form[?],
                                   fieldName: String,
                                   generator: Gen[String],
                                   requiredError: FormError
                                 ): Unit = {
    "must not bind invalid email format" in {
      forAll(generator -> "invalidEmail") { (email: String) =>
        val result = form.bind(Map(fieldName -> email)).apply(fieldName)
        result.errors.toList must contain(requiredError)
      }
    }
  }

  def fieldWithValidEmailformat(form: Form[?], fieldName: String, generator: Gen[String]): Unit = {
    "must bind valid email format" in {
      forAll(generator -> "validEmail") { (email: String) =>
        val result = form.bind(Map(fieldName -> email)).apply(fieldName)
        result.value.value mustBe email
        result.errors mustBe empty
      }
    }
}
}
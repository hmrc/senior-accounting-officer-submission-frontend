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

import org.scalatest.OptionValues
import org.scalatest.compatible.Assertion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.inject.guice.GuiceApplicationBuilder

trait FormSpec extends AnyFreeSpec with Matchers with OptionValues {

  def checkForError(form: Form[?], data: Map[String, String], expectedErrors: Seq[FormError]): Assertion = {

    form
      .bind(data)
      .fold(
        formWithErrors => {
          for error <- expectedErrors do
            formWithErrors.errors must contain(FormError(error.key, error.message, error.args))
          formWithErrors.errors.size mustBe expectedErrors.size
        },
        form => {
          fail("Expected a validation error when binding the form, but it was bound successfully.")
        }
      )
  }

  def error(key: String, value: String, args: Any*): Seq[FormError] = Seq(FormError(key, value, args))

  lazy val emptyForm: Map[String, String] = Map[String, String]()

  lazy val app: Application = GuiceApplicationBuilder().build()
  def messages: Messages    = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  def createTestWithErrorMessageAssertion(key: String, message: String): Unit = {
    s"key $key is in messages file with expected value" in {
      message mustEqual messages(key)
    }
  }
}

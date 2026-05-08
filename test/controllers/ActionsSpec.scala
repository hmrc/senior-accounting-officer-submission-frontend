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

package controllers

import base.SpecBase
import controllers.ActionsSpec.*
import controllers.actions.*
import models.requests.DataRequest
import org.mockito.Mockito.{times, verify}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.mvc.Results.Ok
import play.api.mvc.{ActionBuilder, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future
import scala.util.Random

class ActionsSpec extends SpecBase {

  private def testApplication(): Application = {
    // need to set a none empty user answer to pass the requireData action function
    applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
  }

  def newBlockSpy: BlockSpy = mock[BlockSpy]

  "ensure" - {
    "when the required data exists" - {
      "must the executed block" in {
        val application = testApplication()
        val testValue   = Random.nextInt()

        val blockSpy = newBlockSpy

        running(application) {
          val result = application.commonActions {
            ensure(_ => Some(testValue)) { implicit request => value =>
              blockSpy.spyAction()
              Ok(value.toString)
            }
          }(FakeRequest())

          status(result) mustBe OK
          contentAsString(result) mustBe testValue.toString

          verify(blockSpy, times(1)).spyAction()
        }
      }
    }
    "when the required data does not exists" - {
      "must redirect to Journey Recovery" in {
        val application = testApplication()
        val blockSpy    = newBlockSpy

        running(application) {
          val result = application.commonActions {
            ensure(_ => None) { implicit request => value =>
              blockSpy.spyAction()
              Ok(value.toString)
            }
          }(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.JourneyRecoveryController.onPageLoad().url)

          verify(blockSpy, times(0)).spyAction()
        }
      }
    }
  }

  "ensureAsync" - {
    "when the required data exists" - {
      "must the executed block" in {
        val application = testApplication()
        val testValue   = Random.nextInt()
        val blockSpy    = newBlockSpy

        running(application) {
          val result = application.commonActions.async {
            ensureAsync(_ => Some(testValue)) { implicit request => value =>
              blockSpy.spyAction()
              Future.successful(Ok(value.toString))
            }
          }(FakeRequest())

          status(result) mustBe OK
          contentAsString(result) mustBe testValue.toString

          verify(blockSpy, times(1)).spyAction()
        }
      }
    }

    "when the required data does not exists" - {
      "must redirect to Journey Recovery" in {
        val application = testApplication()
        val blockSpy    = newBlockSpy

        running(application) {
          val result = application.commonActions.async {
            ensureAsync(_ => None) { implicit request => value =>
              blockSpy.spyAction()
              Future.successful(Ok(value.toString))
            }
          }(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.JourneyRecoveryController.onPageLoad().url)

          verify(blockSpy, times(0)).spyAction()
        }
      }
    }
  }

}

object ActionsSpec {
  extension (app: Application) {
    def commonActions: ActionBuilder[DataRequest, AnyContent] = {
      val identify    = app.injector.instanceOf[IdentifierAction]
      val getData     = app.injector.instanceOf[DataRetrievalAction]
      val requireData = app.injector.instanceOf[DataRequiredAction]
      (identify andThen getData andThen requireData)
    }
  }

  class BlockSpy {
    def spyAction(): Unit = ()
  }
}

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

package config

import base.SpecBase
import com.mongodb.MongoException
import models.JourneySection
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class ErrorHandlerSpec extends SpecBase with GuiceOneAppPerSuite {

  private val fakeRequest = FakeRequest("GET", "/")

  private val handler = app.injector.instanceOf[ErrorHandler]

  "standardErrorTemplate must" - {
    "render HTML" in {
      val html = handler.standardErrorTemplate("title", "heading", "message")(fakeRequest).futureValue
      html.contentType mustBe "text/html"
    }
  }

  "NotFoundError must" - {
    "render HTML" in {
      val html = handler.notFoundTemplate(fakeRequest).futureValue
      html.contentType mustBe "text/html"
    }
  }

  "internalServerErrorTemplate must" - {
    "render HTML" in {
      val html = handler.internalServerErrorTemplate(fakeRequest).futureValue
      html.contentType mustBe "text/html"
    }
  }

  "onServerError must" - {

    "redirect to JourneyRecoveryController with the notification section when a MongoException occurs on a notification path" in {
      val request = FakeRequest("GET", "/senior-accounting-officer/submission/notification/check-your-answers")
      val result  = handler.onServerError(request, new MongoException("boom")).futureValue

      result.header.status mustBe SEE_OTHER
      result.header.headers.get(LOCATION) mustBe Some(
        controllers.routes.JourneyRecoveryController
          .onPageLoad(section = Some(JourneySection.Notification.toString))
          .url
      )
    }

    "redirect to JourneyRecoveryController with the certificate section when a MongoException occurs on a certificate path" in {
      val request = FakeRequest("GET", "/senior-accounting-officer/submission/certificate/check-your-answers")
      val result  = handler.onServerError(request, new MongoException("boom")).futureValue

      result.header.headers.get(LOCATION) mustBe Some(
        controllers.routes.JourneyRecoveryController.onPageLoad(section = Some(JourneySection.Certificate.toString)).url
      )
    }

    "redirect to JourneyRecoveryController with no section when a MongoException occurs on an unrelated path" in {
      val request = FakeRequest("GET", "/senior-accounting-officer/submission/some-other-path")
      val result  = handler.onServerError(request, new MongoException("boom")).futureValue

      result.header.headers.get(LOCATION) mustBe Some(
        controllers.routes.JourneyRecoveryController.onPageLoad().url
      )
    }

    "detect a MongoException wrapped inside another exception's cause" in {
      val request      = FakeRequest("GET", "/senior-accounting-officer/submission/notification/check-your-answers")
      val wrappedMongo = new RuntimeException("wrapper", new MongoException("boom"))
      val result       = handler.onServerError(request, wrappedMongo).futureValue

      result.header.headers.get(LOCATION) mustBe Some(
        controllers.routes.JourneyRecoveryController
          .onPageLoad(section = Some(JourneySection.Notification.toString))
          .url
      )
    }

    "fall back to the default error handling for a non-Mongo exception" in {
      val request = FakeRequest("GET", "/senior-accounting-officer/submission/notification/check-your-answers")
      val result  = handler.onServerError(request, new RuntimeException("not mongo related")).futureValue

      result.header.status mustBe INTERNAL_SERVER_ERROR
    }
  }
}

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

package controllers.actions

import base.SpecBase
import config.AppConfig
import play.api.mvc.BodyParsers.Default
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction { _ => Results.Ok }
  }

  val bodyParsers: Default = app.injector.instanceOf[BodyParsers.Default]
  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "Auth Action" when {

    "the user hasn't logged in" must {
      "redirect the user to log in " in {
        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new MissingBearerToken),
          appConfig,
          bodyParsers
        )
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(appConfig.loginContinueUrl)
      }
    }

    "the user's session has expired" must {
      "redirect the user to log in " in {

        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new BearerTokenExpired),
          appConfig,
          bodyParsers
        )
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(appConfig.loginContinueUrl)
      }
    }

    "the user doesn't have sufficient enrolments" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new InsufficientEnrolments),
          appConfig,
          bodyParsers
        )
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe appConfig.hubUnauthorisedUrl
      }
    }

    "the user doesn't have sufficient confidence level" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new InsufficientConfidenceLevel),
          appConfig,
          bodyParsers
        )
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe appConfig.hubUnauthorisedUrl
      }
    }

    "the user used an unaccepted auth provider" must {
      "redirect the user to the unauthorised page" in {

        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new UnsupportedAuthProvider),
          appConfig,
          bodyParsers
        )
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe appConfig.hubUnauthorisedUrl
      }
    }

    "the user has an unsupported affinity group" must {
      "redirect the user to the unauthorised page" in {

        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
          appConfig,
          bodyParsers
        )
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(appConfig.hubUnauthorisedUrl)
      }
    }

    "the user has an unsupported credential role" must {
      "redirect the user to the unauthorised page" in {

        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new UnsupportedCredentialRole),
          appConfig,
          bodyParsers
        )
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(appConfig.hubUnauthorisedUrl)
      }
    }

  }

}

class FakeFailingAuthConnector(exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)
}

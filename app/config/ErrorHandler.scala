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

import com.mongodb.MongoException
import models.JourneySection
import play.api.i18n.MessagesApi
import play.api.mvc.Results.Redirect
import play.api.mvc.{RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.{ErrorTemplate, PageNotFoundView, UnexpectedErrorView}

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.{Inject, Singleton}

@Singleton
class ErrorHandler @Inject() (
    errorTemplate: ErrorTemplate,
    pageNotFoundView: PageNotFoundView,
    unexpectedErrorView: UnexpectedErrorView,
    override val messagesApi: MessagesApi
)(implicit override val ec: ExecutionContext)
    extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit
      request: RequestHeader
  ): Future[Html] =
    Future.successful(errorTemplate(pageTitle, heading, message))

  override def internalServerErrorTemplate(implicit request: RequestHeader): Future[Html] = {
    Future.successful(unexpectedErrorView())
  }

  override def notFoundTemplate(implicit request: RequestHeader): Future[Html] =
    Future.successful(pageNotFoundView())

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] =
    if isMongoFailure(exception) then {
      Future.successful(
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad(section = sectionFor(request.path)))
      )
    } else {
      super.onServerError(request, exception)
    }

  private def isMongoFailure(throwable: Throwable): Boolean =
    throwable match {
      case _: MongoException => true
      case other             => Option(other.getCause).exists(isMongoFailure)
    }

  private def sectionFor(path: String): Option[String] =
    if path.contains("/notification/") then Some(JourneySection.Notification.toString)
    else if path.contains("/certificate/") then Some(JourneySection.Certificate.toString)
    else None
}

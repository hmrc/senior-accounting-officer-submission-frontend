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

package controllers.testonly

import config.AppConfig
import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import play.api.mvc.*
import play.twirl.api.Html
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.StringContextOps
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.testonly.EmailViewerView

import scala.concurrent.ExecutionContext

import javax.inject.Inject

class EmailViewerController @Inject() (
    httpClientV2: HttpClientV2,
    view: EmailViewerView,
    val controllerComponents: MessagesControllerComponents,
    appConfig: AppConfig
)(using
    ExecutionContext
) extends FrontendBaseController {
  def list: Action[AnyContent] = Action.async { implicit request =>
    httpClientV2
      .get(url"${appConfig.digitalContactStubUrl}/digital-contact-stub/imi/messages")
      .execute[HttpResponse]
      .map { response => Json.parse(response.body).as[Seq[JsValue]] }
      .map { json =>
        val reader = (
          (__ \ "to" \ 0 \ "correlationId").read[String] and
            (__ \ "to" \ 0 \ "email").read[Seq[String]].map(_.mkString(", ")) and
            (__ \ "content" \ "subject").read[String]
        ).tupled

        json.map(_.as(reader))
      }
      .map { tuples =>
        tuples.map(tuple => EmailData(correlationId = tuple._1, address = tuple._2, subject = tuple._3))
      }
      .map { response => Ok(view(response)) }
  }

  def render(correlationId: String): Action[AnyContent] = Action.async { implicit request =>
    httpClientV2
      .get(url"${appConfig.digitalContactStubUrl}/digital-contact-stub/imi/messages/$correlationId")
      .execute[HttpResponse]
      .map { response => Json.parse(response.body) }
      .map { json => (json \ 0 \ "content" \ "html").as[String] }
      .map { html => Ok(Html(html)) }
  }
}

final case class EmailData(correlationId: String, address: String, subject: String)

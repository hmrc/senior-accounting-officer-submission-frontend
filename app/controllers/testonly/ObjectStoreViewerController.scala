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

import connectors.InternalAuthTestOnlyConnector
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.Implicits.*
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.testonly.ObjectStoreView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

import javax.inject.Inject

class ObjectStoreViewerController @Inject() (
    objectStoreClient: PlayObjectStoreClient,
    connector: InternalAuthTestOnlyConnector,
    cc: MessagesControllerComponents,
    view: ObjectStoreView
)(using ExecutionContext, Materializer)
    extends FrontendController(cc)
    with Logging {

  def listDirectory(stubInternalAuth: Boolean): Action[AnyContent] = Action.async { implicit request =>
    objectStoreClient
      .listObjects(
        Path.Directory("/"),
        owner = "senior-accounting-officer"
      )
      .map { objectSummaries =>
        Ok(view(objectSummaries.objectSummaries))
      }
      .recoverWith {
        case e @ UpstreamErrorResponse(_, status @ (401 | 403), _, _) if stubInternalAuth =>
          logger.warn(s"objectStoreClient.listObjects failed with $status, attempting to stub")
          connector
            .grantSaoObjectStoreAccess()
            .map(_ =>
              Redirect(
                controllers.testonly.routes.ObjectStoreViewerController.listDirectory(stubInternalAuth = false)
              )
            )
      }
  }

  def download(location: String, fileName: String): Action[AnyContent] = Action.async { implicit request =>
    val fileLocation = Path.Directory(location.replaceFirst("^senior-accounting-officer", "")).file(fileName)
    objectStoreClient
      .getObject[Source[ByteString, NotUsed]](
        fileLocation,
        owner = "senior-accounting-officer"
      )
      .map {
        case Some(obj) =>
          Ok.chunked(
            obj.content,
            inline = false,
            fileName = Some(fileName)
          )
        case None =>
          logger.warn(s"GET ${fileLocation.asUri} not found")
          NotFound
      }
      .recover { case NonFatal(e) =>
        logger.warn(s"GET ${fileLocation.asUri} failed ${e.getMessage}")
        NotFound
      }
  }

  def delete(location: String, fileName: String): Action[AnyContent] = Action.async { implicit request =>
    val fileLocation = Path.Directory(location.replaceFirst("^senior-accounting-officer", "")).file(fileName)
    objectStoreClient
      .deleteObject(
        fileLocation,
        owner = "senior-accounting-officer"
      )
      .map { _ => Redirect(routes.ObjectStoreViewerController.listDirectory()) }
      .recover { case NonFatal(e) =>
        logger.warn(s"DELETE ${fileLocation.asUri} failed ${e.getMessage}")
        NotFound
      }
  }

}

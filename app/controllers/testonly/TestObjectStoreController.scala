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

import controllers.testonly.TestObjectStoreController.*
import controllers.testonly.TestPdfController.*
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.objectstore.client.play.Implicits.*
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import uk.gov.hmrc.objectstore.client.{Object, Path}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.testonly.TestDownloadPdfView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

// will need sm2 --start INTERNAL_AUTH OBJECT_STORE_STUB

class TestObjectStoreController @Inject() (
    config: Configuration,
    mcc: MessagesControllerComponents,
    openHtmlToPdfService: OpenHtmlToPdfService,
    httpClient: HttpClientV2,
    objectStoreClient: PlayObjectStoreClient,
    downloadPdfView: TestDownloadPdfView
)(using ec: ExecutionContext, as: ActorSystem, mat: Materializer)
    extends FrontendController(mcc) {

  private def logger = Logger(getClass)

  // configure the local internal auth for us to talk to object store
  def setUpLocalInternalAuthToken(): Action[AnyContent] = Action.async { implicit request =>
    httpClient
      .post(url"http://localhost:8470/test-only/token")
      .withBody(
        Json.parse(
          s"""|{
              |  "token": "${config.get[String]("internal-auth.token")}",
              |  "principal": "object-store",
              |  "permissions": [{
              |    "resourceType": "object-store",
              |    "resourceLocation": "$service",
              |    "actions": ["*"]
              |  }]
              |}
              |""".stripMargin
        )
      )
      .execute[HttpResponse]
      .map(r => Ok(s"status=${r.status},body=${r.body}"))
  }

  def uploadToObjectStore(): Action[AnyContent] = Action.async { implicit request =>
    val html                           = OpenHtmlToPdfService.testHtml(1000)
    val content: Source[ByteString, ?] = openHtmlToPdfService.builderFor(html).asSource
    objectStoreClient
      .putObject(
        path = Path.Directory(s"$subscriptionId/pdf").file(s"$notificationId.pdf"),
        content = content,
        owner = service
      )
      // this returns the computed md5 for the file we've just uploaded (that'll be required for calling SDES)
      .map { r =>
        logger.error(s"[uploadToObjectStore.md5]=${r.contentMd5.value}")
        Ok(r.toString)
      }
  }

  def downloadFromObjectStore(): Action[AnyContent] = Action.async { implicit request =>
    objectStoreClient
      .getObject[Source[ByteString, NotUsed]](
        path = Path.Directory(s"$subscriptionId/pdf").file(s"$notificationId.pdf"),
        owner = service
      )
      .map {
        case Some(Object(_, source, _)) =>
          Ok.chunked(
            source,
            inline = false,
            fileName = Some(s"$notificationId.pdf")
          )
        case _ => NotFound
      }
  }

  def downloadPdfPage(): Action[AnyContent] = Action.async { implicit request =>
    presignedDownloadUrl.map(url => Ok(downloadPdfView(presignedDownloadUrl = url)))
  }

  private def presignedDownloadUrl(using HeaderCarrier): Future[Option[String]] = objectStoreClient
    .presignedDownloadUrl(
      path = Path.Directory(s"$subscriptionId/pdf").file(s"$notificationId.pdf"),
      owner = service
    )
    .map(url => Some(url.downloadUrl.toString))
    .recover(_ => None)

}

object TestObjectStoreController {
  val service        = "senior-accounting-officer"
  val subscriptionId = "XSAO0000000001"
  val notificationId = "XSAONOTI0000000001"
}

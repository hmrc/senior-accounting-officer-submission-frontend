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

import controllers.testonly.TestObjectStoreController.{notificationId, subscriptionId}
import controllers.testonly.TestSdesController.*
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.writeableOf_JsValue
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.client.{HttpClientV2, readStreamHttpResponse}
import uk.gov.hmrc.http.{HttpResponse, StringContextOps}
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.Path.File
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

import java.util.UUID
import javax.inject.Inject

// sm2 --start --appendArgs '{"FILE_UPLOAD_SDES_STUB":["-Dcallback.recipientOrSender.sao=http://localhost:10058/internal/test-only/sdes/call-back"]}' FILE_UPLOAD_SDES_STUB
class TestSdesController @Inject() (
    mcc: MessagesControllerComponents,
    objectStoreClient: PlayObjectStoreClient,
    httpClientV2: HttpClientV2
)(using ExecutionContext)
    extends FrontendController(mcc) {
  private def logger = Logger(getClass)

  // schema: https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=560334137&spaceKey=SDES&title=File%2BTransfer%2Bfrom%2Bservices%2Brunning%2Bin%2BMDTP%2Bto%2BFTS
  def fileReady: Action[AnyContent] = Action.async { implicit request =>
    def sdesRequestBody(md5: String) = Json.obj(
      "informationType" -> informationType,
      "file"            -> Json.obj(
        "recipientOrSender" -> recipientOrSender,
        "name"              -> fileName,
        "location" -> location, // [UNCERTAIN] not entirely sure what this should be, could also need to be a pre-signed URL
        "checksum" -> Json.obj(
          "algorithm" -> "md5", // one of [md5,SHA-512,SHA-256]
          "value" -> md5 // [UNCERTAIN] object-store returns it in base64 encoding, assuming this is the same format expected by SDES
        ),
        "size"       -> 0,
        "properties" -> Json.arr(
          Json.obj(
            "name"  -> "meta-data",
            "value" -> testMetadata
          )
        )
      ),
      "audit" -> Json.obj("correlationID" -> UUID.randomUUID().toString)
    )

    for {
      // this is done to allow a simpler poc on a distinct route,
      // We can get the md5 from the PUT to object store instead of having to make this call.
      md5 <- objectStoreClient
        .presignedDownloadUrl(path = objectStorePath, owner = TestObjectStoreController.service)
        .map(_.contentMd5)
      sdesResponse <- httpClientV2
        .post(url"http://localhost:10058/notification/fileready") // via our stub -> FILE_UPLOAD_SDES_STUB
        .setHeader(X_CLIENT_ID_HEADER_NAME -> xClientId)
        .withBody(
          sdesRequestBody(md5 = md5.value)
        )
        .execute[HttpResponse]
    } yield {
      logger.error(s"[SDES-fileReady][name]=$fileName")
      logger.error(s"[SDES-fileReady][location]=$location")
      logger.error(s"[SDES-fileReady][md5]=${md5.value}")
      logger.error(s"[SDES-fileReady][json]=${Json.stringify(sdesRequestBody(md5 = md5.value))}")
      Ok(s"status=${sdesResponse.status},body=${sdesResponse.body}")
    }
  }

  def sdesProxyStub(): Action[JsValue] = Action(parse.json).async { implicit request =>
    //  based off of
    //  https://github.com/hmrc/secure-data-exchange-proxy/blob/main/app/uk/gov/hmrc/sdes/proxy/actions/FileTypeRefiner.scala#L16
    //  https://github.com/hmrc/secure-data-exchange-proxy/blob/main/app/uk/gov/hmrc/sdes/proxy/validators/ClientIdHeaderValidator.scala#L23
    logger.error(
      s"[SDES-Proxy] $X_CLIENT_ID_HEADER_NAME=${request.headers.get(X_CLIENT_ID_HEADER_NAME)}, body=${request.body}"
    )
    ((request.body \ "informationType").asOpt[String], request.headers.get(X_CLIENT_ID_HEADER_NAME)) match {
      case (None, _) => Future.successful(BadRequest("A valid informationType must be provided"))
      case (Some(`informationType`), Some(`xClientId`)) =>
        httpClientV2
          .post(url"http://localhost:9191/sdes-stub/notification/fileready") // FILE_UPLOAD_SDES_STUB
          .withBody(request.body)
          .execute[HttpResponse]
          .map(r => Status(r.status)(r.body))
      case (Some(`informationType`), Some(blank)) if blank.trim.isEmpty =>
        logger.error(s"Client-Id '$X_CLIENT_ID_HEADER_NAME' has a blank value '$blank'.")
        Future.successful(Unauthorized)
      case (Some(fileType), Some(unmatchedClientId)) =>
        logger.error(s"Client-Id '$unmatchedClientId' is forbidden to request filetype '$fileType'.")
        Future.successful(Forbidden)
      case _ =>
        logger.error(s"Header '$X_CLIENT_ID_HEADER_NAME' not found in the request.")
        Future.successful(Unauthorized)
    }

  }

  def callBack(): Action[AnyContent] = Action { implicit request =>
    // info on possible stubbed response scenarios:
    // https://github.com/hmrc/sdes-stub/blob/c3c8472ef6926ea0d7ba7bee033096b1d8f8864c/app/uk/gov/hmrc/sdesstub/controller/SDESController.scala#L69
    // https://github.com/hmrc/sdes-stub/blob/c3c8472ef6926ea0d7ba7bee033096b1d8f8864c/app/uk/gov/hmrc/sdesstub/service/CallbackScheduler.scala#L27

    // case 1 transfer was successful (2 async callbacks)
    // FileReceived and then FileProcessed

    // case 2 file received (1 async callback)
    // FileReceived only
    // TODO what do we do in this case? how do we identify it? some sort of timeout?

    // case 3 file is unavailable (1 async callback)
    // FileProcessingFailure("Unable to download file")

    // case 4 file contains a virus (2 async callbacks)
    // FileReceived and then FileProcessingFailure("File rejected, virus detected")

    logger.error(s"[SDES-callback]${request.body.asJson.get}")
    Ok
  }
}

object TestSdesController {

  // https://github.com/hmrc/secure-data-exchange-proxy/blob/main/app/uk/gov/hmrc/sdes/proxy/validators/ClientIdHeaderValidator.scala#L48
  val X_CLIENT_ID_HEADER_NAME = "X-Client-ID"
  val fileName: String        = s"$notificationId.pdf"
  val objectStorePath: File   = Path.Directory(s"$subscriptionId/pdf").file(fileName)
  def location: String        = "/object-store/" + objectStorePath.asUri

  // I suspect all of these will be confirmed as part of SDES onboarding
  // the xClientId & informationType are default configs accepted in sdes proxy started from sm2
  val xClientId         = "xClientId" // auth token
  val informationType   = "informationType"
  val recipientOrSender = "sao"       // n.b. we need this to match the callback config for the FILE_UPLOAD_SDES_STUB

  // metadata from the new API
  val testMetadata = "some-value-from-documentum"
}

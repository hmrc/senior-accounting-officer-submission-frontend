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

package repository

import com.mongodb.client.model
import models.*
import org.bson.types.ObjectId
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.{set, setOnInsert}
import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.formats.MongoFormats
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.{Inject, Singleton}

object UserSessionRepository:

  private val statusType           = "statusType"
  private val InProgress           = "InProgress"
  private val Failed               = "Failed"
  private val UploadedSuccessfully = "UploadedSuccessfully"

  given Format[UploadStatus] =

    given Format[UploadStatus.UploadedSuccessfully] = Json.format[UploadStatus.UploadedSuccessfully]

    val read: Reads[UploadStatus] = (json: JsValue) =>
      json match {
        case jsObject: JsObject =>
          jsObject.value.get(statusType) match
            case Some(JsString(InProgress))           => JsSuccess(UploadStatus.InProgress)
            case Some(JsString(Failed))               => JsSuccess(UploadStatus.Failed)
            case Some(JsString(UploadedSuccessfully)) => Json.fromJson[UploadStatus.UploadedSuccessfully](jsObject)
            case Some(value)                          => JsError(s"Unexpected value of statusType: $value")
            case None                                 => JsError("Missing statusType field")
        case other => JsError(s"Expected a JsObject but got ${other.getClass.getSimpleName}")
      }

    val write: Writes[UploadStatus] =
      (p: UploadStatus) =>
        p fold (
          ifInProgress = JsObject(Map(statusType -> JsString(InProgress))),
          ifFailed = JsObject(Map(statusType -> JsString(Failed))),
          ifSuccess = s =>
            Json.toJson(s).as[JsObject]
              + (statusType -> JsString(UploadedSuccessfully))
        )

    Format(read, write)

  private given Format[UploadId] = Json.valueFormat[UploadId]

  private[repository] val mongoFormat: Format[FileUploadState] =
    given Format[ObjectId] = MongoFormats.objectIdFormat
    ((__ \ "_id").format[ObjectId]
      ~ (__ \ "uploadId").format[UploadId]
      ~ (__ \ "reference").format[UpscanFileReference]
      ~ (__ \ "status").format[UploadStatus])(FileUploadState.apply, Tuple.fromProductTyped _)

@Singleton
class UserSessionRepository @Inject() (
    mongoComponent: MongoComponent
)(using
    ExecutionContext
) extends PlayMongoRepository[FileUploadState](
      collectionName = "UpscanResultTrackerRepository",
      mongoComponent = mongoComponent,
      domainFormat = UserSessionRepository.mongoFormat,
      indexes = Seq(
        IndexModel(Indexes.ascending("uploadId"), IndexOptions().unique(true)),
        IndexModel(Indexes.ascending("reference"), IndexOptions().unique(true))
      ),
      replaceIndexes = true
    ):
  import UserSessionRepository.given

  override lazy val requiresTtlIndex: Boolean = false

  def insert(details: FileUploadState): Future[Unit] =
    collection
      .insertOne(details)
      .toFuture()
      .map(_ => ())

  def findByUploadId(uploadId: UploadId): Future[Option[FileUploadState]] =
    collection.find(equal("uploadId", Codecs.toBson(uploadId))).headOption()

  def updateStatus(reference: UpscanFileReference, newStatus: UploadStatus): Future[UploadStatus] =
    collection
      .findOneAndUpdate(
        filter = equal("reference", Codecs.toBson(reference)),
        update = Updates.combine(
          set("status", Codecs.toBson(newStatus)),
          setOnInsert("uploadId", Codecs.toBson(UploadId.generate())),
          setOnInsert("_id", ObjectId.get())
        ),
        options = FindOneAndUpdateOptions().upsert(true).returnDocument(model.ReturnDocument.AFTER)
      )
      .toFuture()
      .map(_.status)

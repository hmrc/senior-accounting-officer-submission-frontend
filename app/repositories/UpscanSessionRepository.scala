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

package repositories

import com.mongodb.client.model
import config.AppConfig
import models.*
import org.bson.types.ObjectId
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.{set, setOnInsert}
import play.api.libs.json.*
import uk.gov.hmrc.mdc.Mdc
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import scala.concurrent.{ExecutionContext, Future}

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

@Singleton
class UpscanSessionRepository @Inject() (
    appConfig: AppConfig,
    mongoComponent: MongoComponent,
    clock: Clock
)(using
    ExecutionContext
) extends PlayMongoRepository[FileUploadState](
      collectionName = "upscan-result-tracker",
      mongoComponent = mongoComponent,
      domainFormat = FileUploadState.mongoFormat,
      indexes = Seq(
        IndexModel(Indexes.ascending("reference"), IndexOptions().unique(true)),
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS)
        )
      ),
      replaceIndexes = true
    ) {

  def keepAlive(id: UpscanFileReference): Future[Boolean] = Mdc.preservingMdc {
    collection
      .updateOne(
        filter = Filters.equal("reference", id.reference),
        update = Updates.set("lastUpdated", Instant.now(clock))
      )
      .toFuture()
      .map(_ => true)
  }

  def insert(details: FileUploadState): Future[Boolean] = Mdc.preservingMdc {
    collection
      .insertOne(details)
      .toFuture()
      .map(_ => true)
  }

  def find(reference: UpscanFileReference): Future[Option[FileUploadState]] = Mdc.preservingMdc {
    keepAlive(reference).flatMap(_ => collection.find(equal("reference", Codecs.toBson(reference))).headOption())
  }

  def updateStatus(reference: UpscanFileReference, newStatus: UploadStatus): Future[UploadStatus] = Mdc.preservingMdc {
    collection
      .findOneAndUpdate(
        filter = equal("reference", Codecs.toBson(reference)),
        update = Updates.combine(
          set("status", Codecs.toBson(newStatus)),
          setOnInsert("_id", ObjectId.get()),
          setOnInsert("lastUpdated", Instant.now(clock))
        ),
        options = FindOneAndUpdateOptions().upsert(true).returnDocument(model.ReturnDocument.AFTER)
      )
      .toFuture()
      .map(_.status)
  }
}

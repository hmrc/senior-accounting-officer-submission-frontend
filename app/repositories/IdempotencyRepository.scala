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

package repositories

import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.MongoComponent
import scala.concurrent.ExecutionContext
import models.IdempotencyId
import org.mongodb.scala.model.*
import java.util.UUID
import scala.concurrent.Future
import uk.gov.hmrc.mdc.Mdc
import javax.inject.Inject
import org.bson.codecs.UuidCodec
import org.bson.UuidRepresentation
import java.util.concurrent.TimeUnit
import config.AppConfig
import java.time.Instant

class IdempotencyRepository @Inject() (mongoComponent: MongoComponent, appConfig: AppConfig)(using ec: ExecutionContext)
    extends PlayMongoRepository[IdempotencyId](
      collectionName = "idempotency-ids",
      mongoComponent = mongoComponent,
      domainFormat = IdempotencyId.format,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("id"),
          IndexOptions().name("id").unique(true).sparse(true)
        ),
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS)
        )
      ),
      extraCodecs = Seq(new UuidCodec(UuidRepresentation.STANDARD))
    ) {
  def insert(id: UUID): Future[Boolean] = Mdc.preservingMdc {
    collection
      .find(Filters.equal("id", id.toString()))
      .headOption
      .flatMap(option =>
        option.fold(
          {
            collection.insertOne(IdempotencyId(id, Instant.now)).toFuture().map(_ => true)
          }
        )(_ => Future.successful(false))
      )
  }
}

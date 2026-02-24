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

import base.SpecBase
import config.AppConfig
import models.*
import models.FileUploadState.mongoFormat
import org.bson.types.ObjectId
import org.mockito.Mockito.when
import org.mongodb.scala.model.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global

class UpscanSessionRepositorySpec
    extends SpecBase
    with DefaultPlayMongoRepositorySupport[FileUploadState]
    with ScalaFutures
    with MockitoSugar {

  private val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1L

  private val instant                              = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock                     = Clock.fixed(instant, ZoneId.systemDefault)
  override val repository: UpscanSessionRepository = UpscanSessionRepository(mockAppConfig, mongoComponent, stubClock)

  "UpscanSessionRepository must" - {
    "insert, findByUploadId, and updateStatus" in {
      val reference = "foo"
      val details   = FileUploadState(
        _id = ObjectId.get(),
        reference = reference,
        status = UploadStatus.InProgress,
        instant
      )

      // Insert and find
      repository.insert(details).futureValue
      val found = repository.find(reference).futureValue
      found mustBe Some(details)

      val newStatus = UploadStatus.UploadedSuccessfully(
        name = "test.pdf",
        mimeType = "application/pdf",
        downloadUrl = "http://localhost:8080/download",
        size = Some(123)
      )
      val updated = repository.updateStatus(reference, newStatus).futureValue
      updated mustBe newStatus

      val foundAfterUpdate = repository.find(reference).futureValue
      foundAfterUpdate.get.status mustBe newStatus
    }

    "return None when findByUploadId is called with a non-existent id" in {
      val reference = "foo"
      val result    = repository.find(reference).futureValue
      result mustBe None
    }

    "upsert a new document when updateStatus is called with a non-existent reference" in {
      val reference = "bar"
      val newStatus = UploadStatus.Failed

      repository.collection.find(Filters.equal("reference", reference)).headOption().futureValue mustBe None

      val updated = repository.updateStatus(reference, newStatus).futureValue
      updated mustBe newStatus

      val found = repository.collection
        .find(Filters.equal("reference", reference))
        .headOption()
        .futureValue
      found must not be empty
      found.get.status mustBe newStatus

    }

    "serialize and deserialize InProgress status" in {
      val input =
        FileUploadState(
          _id = ObjectId.get(),
          reference = "ABC",
          status = UploadStatus.InProgress,
          lastUpdated = instant
        )

      val serialized = mongoFormat.writes(input)
      val output     = mongoFormat.reads(serialized)

      output.get mustBe input
    }

    "serialize and deserialize Failed status" in {
      val input =
        FileUploadState(ObjectId.get(), "ABC", UploadStatus.Failed, instant)

      val serialized = mongoFormat.writes(input)
      val output     = mongoFormat.reads(serialized)

      output.get mustBe input
    }

    "serialize and deserialize UploadedSuccessfully status when size is unknown" in {
      val input = FileUploadState(
        _id = ObjectId.get(),
        reference = "ABC",
        status = UploadStatus.UploadedSuccessfully("foo.txt", "text/plain", "http:localhost:8080", size = None),
        lastUpdated = instant
      )

      val serialized = mongoFormat.writes(input)
      val output     = mongoFormat.reads(serialized)

      output.get mustBe input
    }

    "serialize and deserialize UploadedSuccessfully status when size is known" in {
      val input = FileUploadState(
        _id = ObjectId.get(),
        reference = "ABC",
        status = UploadStatus.UploadedSuccessfully("foo.txt", "text/plain", "http:localhost:8080", size = Some(123456)),
        lastUpdated = instant
      )

      val serialized = mongoFormat.writes(input)
      val output     = mongoFormat.reads(serialized)

      output.get mustBe input
    }
  }

}

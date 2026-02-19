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

class UserSessionRepositorySpec
    extends SpecBase
    with DefaultPlayMongoRepositorySupport[FileUploadState]
    with ScalaFutures
    with MockitoSugar {

  private val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1L

  private val instant                              = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock                     = Clock.fixed(instant, ZoneId.systemDefault)
  override val repository: UpscanSessionRepository = UpscanSessionRepository(mockAppConfig, mongoComponent, stubClock)

  "UserSessionRepository must" - {
    "insert, findByUploadId, and updateStatus" in {
      val uploadId  = UploadId.generate()
      val reference = UpscanFileReference("foo")
      val details   = FileUploadState(
        `_id` = ObjectId.get(),
        uploadId = uploadId,
        reference = reference,
        status = UploadStatus.InProgress,
        instant
      )

      // Insert and find
      repository.insert(details).futureValue
      val found = repository.findByUploadId(uploadId).futureValue
      found mustBe Some(details)

      val newStatus = UploadStatus.UploadedSuccessfully(
        "test.pdf",
        "application/pdf",
        "http://localhost:8080/download",
        Some(123)
      )
      val updated = repository.updateStatus(reference, newStatus).futureValue
      updated mustBe newStatus

      val foundAfterUpdate = repository.findByUploadId(uploadId).futureValue
      foundAfterUpdate.get.status mustBe newStatus
    }

    "return None when findByUploadId is called with a non-existent id" in {
      val uploadId = UploadId.generate()
      val result   = repository.findByUploadId(uploadId).futureValue
      result mustBe None
    }

    "upsert a new document when updateStatus is called with a non-existent reference" in {
      val reference = UpscanFileReference("bar")
      val newStatus = UploadStatus.Failed

      repository.collection.find(Filters.equal("reference", reference.reference)).headOption().futureValue mustBe None

      val updated = repository.updateStatus(reference, newStatus).futureValue
      updated mustBe newStatus

      val found = repository.collection
        .find(Filters.equal("reference", reference.reference))
        .headOption()
        .futureValue
      found must not be empty
      found.get.status mustBe newStatus

    }

    "serialize and deserialize InProgress status" in:
      val input =
        FileUploadState(
          ObjectId.get(),
          UploadId.generate(),
          UpscanFileReference("ABC"),
          UploadStatus.InProgress,
          instant
        )

      val serialized = mongoFormat.writes(input)
      val output     = mongoFormat.reads(serialized)

      output.get mustBe input

    "serialize and deserialize Failed status" in:
      val input =
        FileUploadState(ObjectId.get(), UploadId.generate(), UpscanFileReference("ABC"), UploadStatus.Failed, instant)

      val serialized = mongoFormat.writes(input)
      val output     = mongoFormat.reads(serialized)

      output.get mustBe input

    "serialize and deserialize UploadedSuccessfully status when size is unknown" in:
      val input = FileUploadState(
        ObjectId.get(),
        UploadId.generate(),
        UpscanFileReference("ABC"),
        UploadStatus.UploadedSuccessfully("foo.txt", "text/plain", "http:localhost:8080", size = None),
        instant
      )

      val serialized = mongoFormat.writes(input)
      val output     = mongoFormat.reads(serialized)

      output.get mustBe input

    "serialize and deserialize UploadedSuccessfully status when size is known" in:
      val input = FileUploadState(
        ObjectId.get(),
        UploadId.generate(),
        UpscanFileReference("ABC"),
        UploadStatus.UploadedSuccessfully("foo.txt", "text/plain", "http:localhost:8080", size = Some(123456)),
        instant
      )

      val serialized = mongoFormat.writes(input)
      val output     = mongoFormat.reads(serialized)

      output.get mustBe input
  }

}

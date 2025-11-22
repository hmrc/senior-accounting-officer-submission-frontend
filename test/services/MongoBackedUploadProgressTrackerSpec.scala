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

package services

import models.*
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import repository.UserSessionRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class MongoBackedUploadProgressTrackerSpec
    extends AnyWordSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[FileUploadState]
    with IntegrationPatience:

  override val repository: UserSessionRepository = UserSessionRepository(mongoComponent)

  val progressTracker: UpscanMongoBackedUploadProgressTracker = UpscanMongoBackedUploadProgressTracker(repository)

  "MongoBackedUploadProgressTracker" should:
    "coordinate workflow" in:
      val reference      = UpscanFileReference("reference")
      val id             = UploadId("upload-id")
      val expectedStatus = UploadStatus.UploadedSuccessfully("name", "mimeType", "downloadUrl", size = Some(123))

      progressTracker.initialiseUpload(id, reference).futureValue
      progressTracker.updateUploadStatus(reference, expectedStatus).futureValue

      val result = progressTracker.getUploadStatus(id).futureValue

      result shouldBe Some(expectedStatus)

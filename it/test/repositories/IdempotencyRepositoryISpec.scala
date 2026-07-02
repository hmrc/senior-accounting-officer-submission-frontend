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

import org.scalatest.freespec.AnyFreeSpec
import models.IdempotencyId
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import java.util.UUID
import IdempotencyRepositoryISpec.*
import org.scalatest.matchers.must.Matchers
import config.AppConfig
import org.mockito.Mockito.when
import org.scalactic.source.Position
import org.scalatestplus.mockito.MockitoSugar
import java.time.Instant

class IdempotencyRepositoryISpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[IdempotencyId]
    with MockitoSugar {

  private val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1L

  protected override val repository: IdempotencyRepository =
    new IdempotencyRepository(mongoComponent = mongoComponent, appConfig = mockAppConfig)(using
      scala.concurrent.ExecutionContext.Implicits.global
    )

  ".insert" - {

    "must add the unused IdempotencyId to the repository and return true" in {
      val result = repository.insert(exampleIdempotencyId).futureValue
      result mustEqual true
    }

    "must not add the unused IdempotencyId to the repository and return false" in {
      insert(IdempotencyId(exampleIdempotencyId, Instant.now)).futureValue
      val result = repository.insert(exampleIdempotencyId).futureValue
      result mustEqual false
    }

  }
}

object IdempotencyRepositoryISpec {
  val exampleIdempotencyId = UUID.randomUUID()
}

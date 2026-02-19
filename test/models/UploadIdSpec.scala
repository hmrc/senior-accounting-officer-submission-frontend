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

package models

import base.SpecBase
import play.api.mvc.QueryStringBindable

class UploadIdSpec extends SpecBase {
  "UploadId.queryBinder must" - {

    val binder: QueryStringBindable[UploadId] = UploadId.queryBinder

    "bind a valid UUID from a query string" in {
      val uploadId = UploadId.generate()
      val params   = Map("uploadId" -> Seq(uploadId.value))
      val result   = binder.bind("uploadId", params)
      result mustBe Some(Right(uploadId))
    }

    "unbind an UploadId into a query string" in {
      val uploadId = UploadId.generate()
      val result   = binder.unbind("uploadId", uploadId)
      result mustBe s"uploadId=${uploadId.value}"
    }

    "return None when the key is not present in the query string" in {
      val params = Map("otherKey" -> Seq("someValue"))
      val result = binder.bind("uploadId", params)
      result mustBe None
    }
  }
}

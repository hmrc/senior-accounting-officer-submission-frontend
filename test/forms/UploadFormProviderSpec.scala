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

package forms

import forms.behaviours.FieldBehaviours
import org.scalacheck.Gen

class UploadFormProviderSpec extends FieldBehaviours {

  "syncErrorKey" - {
    UploadFormProvider.KNOWN_ERROR_CODES.foreach { knownCode =>
      s"must return upload.error.$knownCode for $knownCode" in {
        UploadFormProvider.syncErrorKey(knownCode) mustBe s"upload.error.$knownCode"
      }
    }

    "must return upload.error.unknown for anything that is not EntityTooLarge" in {
      forAll(Gen.alphaStr) { string =>
        whenever(!UploadFormProvider.KNOWN_ERROR_CODES.contains(string)) {
          UploadFormProvider.syncErrorKey(string) mustBe "upload.error.unknown"
        }
      }
    }
  }

  "error message keys must map to the expected text" - {
    createTestWithErrorMessageAssertion(
      key = "upload.error.quarantine",
      message = "The selected file contains a virus"
    )

    createTestWithErrorMessageAssertion(
      key = "upload.error.rejected",
      message = "The selected file must be a CSV"
    )

    createTestWithErrorMessageAssertion(
      key = "upload.error.EntityTooLarge",
      message = "The selected file must be smaller than 100MB"
    )

    createTestWithErrorMessageAssertion(
      key = "upload.error.unknown",
      message = "The selected file could not be uploaded – try again"
    )
  }
}

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

package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class SubmissionDocumentTypeSpec extends AnyFreeSpec with Matchers {

  "SubmissionDocumentType" - {
    "pdfFileName" - {
      "must build the notification file name stored by the backend" in {
        SubmissionDocumentType.Notification.pdfFileName("NOT0123456789") mustBe
          "NOT0123456789_SAO_Notification.pdf"
      }

      "must build the certificate file name stored by the backend" in {
        SubmissionDocumentType.Certificate.pdfFileName("CRT0001234567") mustBe
          "CRT0001234567_SAO_Certificate.pdf"
      }
    }
  }
}

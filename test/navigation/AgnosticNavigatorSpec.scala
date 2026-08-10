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

package navigation

import base.SpecBase
import controllers.certificate.routes as certificateRoutes
import controllers.notification.routes as notificationRoutes
import models.*
import models.certificate.CertificateTaskListStage
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.*

class AgnosticNavigatorSpec extends SpecBase with GuiceOneAppPerSuite {

  lazy val navigator: Navigator = app.injector.instanceOf[AgnosticNavigator]

  "AgnosticNavigator.nextPage" - {

    "in Normal mode" - {

      "must throw an not-implemented error for an unspecified configuration" in {
        case object UnknownPage extends Page
        intercept[NotImplementedError] {
          navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers)
        }
      }

      "SubmissionTypePage" - {

        "when on SubmissionTypePage and the user chose notification only, must go to NotificationTaskList" in {
          navigator.nextPage(
            SubmissionTypePage,
            NormalMode,
            emptyUserAnswers.set(SubmissionTypePage, SubmissionType.Notification).get
          ) mustBe notificationRoutes.NotificationTaskListController.onPageLoad()
        }

        "when on SubmissionTypePage and the user chose certificate only, must go to CertificateTaskList" in {
          navigator.nextPage(
            SubmissionTypePage,
            NormalMode,
            emptyUserAnswers.set(SubmissionTypePage, SubmissionType.Certificate).get
          ) mustBe certificateRoutes.CertificateTaskListController.onPageLoad(
            CertificateTaskListStage.ProvideSaoDetailsStage
          )
        }

        "when on SubmissionTypePage and the user chose both the notification and the certificate, must throw an exception" in {
          intercept[NotImplementedError] {
            navigator.nextPage(
              SubmissionTypePage,
              NormalMode,
              emptyUserAnswers.set(SubmissionTypePage, SubmissionType.Combined).get
            )
          }
        }
      }
    }

    "in Check mode" - {

      "must throw an not-implemented error for an unspecified configuration" in {
        case object UnknownPage extends Page
        intercept[NotImplementedError] {
          navigator.nextPage(UnknownPage, CheckMode, emptyUserAnswers)
        }
      }

    }
  }
}

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

package services
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrlPolicy.Id
import org.mockito.ArgumentMatchers.{any, eq as meq}
import play.api.test.Helpers.*
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import base.SpecBase
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import pages.notification.*
import models.upload.UploadTemplateTableData
import org.scalatestplus.mockito.MockitoSugar.mock
import connectors.ProtectedServiceConnector
import org.mockito.Mockito.when
import uk.gov.hmrc.http.HttpResponse
import play.api.test.Helpers.CREATED
import play.api.inject.bind
import play.api.libs.json.Json

class NotificationSubmitServiceSpec extends SpecBase with GuiceOneAppPerSuite {

  // TODO: separately test mapping code
  // TODO: what testing needs to be done to the connector? integration testing?

  "NotificationSubmitService.submit" - {

    given HeaderCarrier = HeaderCarrier()

    val userAnswers = emptyUserAnswers
      .set(NotificationMoreThanOneSaoPage, false)
      .success
      .value
      .set(NotificationSingleSaoOfficerNamePage, "Jackson Brown")
      .success
      .value
      .set(UploadTemplateTablePage, UploadTemplateTableData(rows = Seq.empty, errors = Seq.empty))
      .success
      .value

    "must return notification response on success" in {
      // TODO: mock the connector so that it is successful
      val mockConnector = mock[ProtectedServiceConnector]
      when(mockConnector.postNotification(any())(using any[HeaderCarrier]())) thenReturn Future.successful(
        HttpResponse(CREATED, Json.obj("notificationReference" -> "test").toString, Map())
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[ProtectedServiceConnector].toInstance(mockConnector)
        )
        .build()

      running(application) {
        val SUT    = application.injector.instanceOf[NotificationSubmitService]
        val result = SUT.submit(userAnswers)
        Await.ready(result, Duration.Inf) mustBe Right(NotificationResponse(notificationReference = "apple"))
      }
    }

    "must return error on mongo failure" in {
      val SUT    = app.injector.instanceOf[NotificationSubmitService]
      val result = SUT.submit(userAnswers)
      Await.ready(result, Duration.Inf) mustBe Left(NotificationSubmissionError.MongoError)
    }

    "must return error on http failure" in {
      val SUT    = app.injector.instanceOf[NotificationSubmitService]
      val result = SUT.submit(userAnswers)
      Await.ready(result, Duration.Inf) mustBe Left(NotificationSubmissionError.HttpError)
    }
  }
}

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

package base

import controllers.actions.*
import models.UserAnswers
import models.upload.UploadTemplateTableData
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.*
import pages.notification.*
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest

import java.time.LocalDate

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience {

  val userAnswersId: String         = "id"
  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def completedSaoDetailsAnswers: UserAnswers =
    emptyUserAnswers
      .set(NotificationMoreThanOneSaoPage, false)
      .success
      .value
      .set(OneSaoSubmitNotificationFullNamePage, "Jackson Brown")
      .success
      .value

  def completedNotificationUploadAnswers: UserAnswers =
    completedSaoDetailsAnswers
      .set(UploadTemplateTablePage, UploadTemplateTableData(rows = Seq.empty, errors = Seq.empty))
      .success
      .value

  def completedMultipleSaoDetailsAnswers: UserAnswers =
    emptyUserAnswers
      .set(NotificationMoreThanOneSaoPage, true)
      .success
      .value
      .set(NotificationMultiSaoLastOfficerNamePage, "Jackson Brown")
      .success
      .value
      .set(NotificationMultiSaoLastOfficerStartDatePage, LocalDate.of(2024, 1, 1))
      .success
      .value
      .set(NotificationMultiSaoPreviousOfficerNamePage(0), "Taylor Green")
      .success
      .value
      .set(NotificationMultiSaoPreviousOfficerStartDatePage(0), LocalDate.of(2023, 1, 1))
      .success
      .value
      .set(NotificationMoreSaoSecondEndDatePage(0), LocalDate.of(2023, 12, 31))
      .success
      .value
      .set(NotificationMoreSaoAreAllAddedPage(0), true)
      .success
      .value

  def userAnswersWithCertificateSaoDetails: UserAnswers =
    emptyUserAnswers
      .set(CertificateSaoFullNamePage, "Firstname Lastname")
      .success
      .value
      .set(CertificateSaoEmailPage, "firstname.lastname@example.com")
      .success
      .value

  def userAnswersWithCertificateUploadedTemplate: UserAnswers =
    userAnswersWithCertificateSaoDetails
      .set(CertificateReviewQualifiedPage, "HACK")
      .success
      .value
      .set(CertificateReviewUnqualifiedPage, "HACK")
      .success
      .value

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )
}

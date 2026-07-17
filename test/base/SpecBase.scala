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
import models.*
import models.upload.UploadTemplateTableData
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.*
import pages.certificate.*
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

  val userAnswersId: String = "id"
  val etmpSafeId            = "etmpSafeId"
  val companyCrn            = "companyCrn"
  val companyName           = "companyName"
  val companyUtr            = "companyUtr"
  val contact1Name          = "Contact 1 Name"
  val contact1Email         = "1@test.com"
  val contact1Language      = "en"
  val contact1Status        = "valid"
  val contact2Name          = "Contact 2 Name"
  val contact2Email         = "2@test.com"
  val contact2Language      = "cy"
  val contact2Status        = "unreachable"

  val subscription: SaoSubscription = SaoSubscription(
    etmpSafeId = etmpSafeId,
    nominatedCompany = NominatedCompany(
      name = companyName,
      crn = companyCrn,
      utr = companyUtr
    ),
    contacts = List(
      Contact(name = contact1Name, email = contact1Email, language = contact1Language, status = contact1Status),
      Contact(name = contact2Name, email = contact2Email, language = contact2Language, status = contact2Status)
    )
  )

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId, subscription)

  def completedSaoDetailsAnswers: UserAnswers =
    emptyUserAnswers
      .set(NotificationMoreThanOneSaoPage, false)
      .success
      .value
      .set(NotificationSingleSaoOfficerNamePage, "Jackson Brown")
      .success
      .value

  def completedNotificationUploadAnswers: UserAnswers =
    completedSaoDetailsAnswers
      .set(UploadTemplateTablePage, UploadTemplateTableData(rows = Seq.empty, errors = Seq.empty))
      .success
      .value

  def completedNotificationReviewAnswers: UserAnswers =
    completedNotificationUploadAnswers
      .set(UploadTemplateReviewPage, true)
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
      .set(NotificationMultiSaoPreviousOfficerEndDatePage(0), LocalDate.of(2023, 12, 31))
      .success
      .value
      .set(NotificationMultiSaoAreAllAddedPage(0), true)
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
      .set(CertificateReviewQualifiedPage, true)
      .success
      .value
      .set(CertificateReviewUnqualifiedPage, true)
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

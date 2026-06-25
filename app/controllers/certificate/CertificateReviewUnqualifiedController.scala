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

package controllers.certificate

import controllers.actions.*
import controllers.routes
import models.NormalMode
import models.upload.*
import navigation.Navigator
import pages.{CertificateReviewUnqualifiedPage, CertificateSaoFullNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.certificate.CertificateReviewUnqualifiedView

import scala.concurrent.{ExecutionContext, Future}

import java.time.LocalDate
import javax.inject.Inject

class CertificateReviewUnqualifiedController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    requireUploadSubmissionTemplateStageUnlocked: RequireCertificateUploadSubmissionTemplateUnlockedAction,
    val controllerComponents: MessagesControllerComponents,
    view: CertificateReviewUnqualifiedView,
    navigator: Navigator,
    sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireUploadSubmissionTemplateStageUnlocked) {
      implicit request =>
        // TODO: remove in future
        val dummyDate            = "2020"
        val unqualifiedDummyData = Seq(
          ParsedSubmissionRow(
            notification = NotificationFields(
              companyName = "example company name",
              companyUtr = CompanyUtr("example company utr"),
              companyCrn = Some(CompanyCrn("example company crn")),
              companyType = CompanyType.LTD,
              companyStatus = CompanyStatus.Administration,
              financialYearEndDate = LocalDate.now()
            ),
            certificate = CertificateFields(
              corporationTax = false,
              valueAddedTax = true,
              paye = false,
              insurancePremiumTax = true,
              stampDutyLandTax = false,
              stampDutyReserveTax = false,
              petroleumRevenueTax = true,
              customsDuties = false,
              exciseDuties = false,
              bankLevy = false,
              certificateType = Some(CertificateType.Unqualified),
              additionalInformation = Some("example additional information")
            )
          ),
          ParsedSubmissionRow(
            notification = NotificationFields(
              companyName = "example company name 2",
              companyUtr = CompanyUtr("example company utr 2"),
              companyCrn = Some(CompanyCrn("example company crn 2")),
              companyType = CompanyType.LTD,
              companyStatus = CompanyStatus.Dormant,
              financialYearEndDate = LocalDate.now()
            ),
            certificate = CertificateFields(
              corporationTax = false,
              valueAddedTax = false,
              paye = false,
              insurancePremiumTax = false,
              stampDutyLandTax = false,
              stampDutyReserveTax = false,
              petroleumRevenueTax = false,
              customsDuties = false,
              exciseDuties = false,
              bankLevy = false,
              certificateType = Some(CertificateType.Unqualified),
              additionalInformation = Some("example additional information ")
            )
          ),
          ParsedSubmissionRow(
            notification = NotificationFields(
              companyName = "example company name 3",
              companyUtr = CompanyUtr("example company utr 3"),
              companyCrn = Some(CompanyCrn("example company crn 3")),
              companyType = CompanyType.LTD,
              companyStatus = CompanyStatus.Active,
              financialYearEndDate = LocalDate.now()
            ),
            certificate = CertificateFields(
              corporationTax = false,
              valueAddedTax = false,
              paye = false,
              insurancePremiumTax = false,
              stampDutyLandTax = false,
              stampDutyReserveTax = false,
              petroleumRevenueTax = false,
              customsDuties = false,
              exciseDuties = false,
              bankLevy = false,
              certificateType = Some(CertificateType.Unqualified),
              additionalInformation = Some("example additional information 3")
            )
          )
        )

        val unqualifiedCompanies = unqualifiedDummyData.map(_.toUnqualifiedCompany)
//TODO: pass financial year end date through for paragraph3
        request.userAnswers
          .get(CertificateSaoFullNamePage)
          .fold(
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          )(saoName =>
            Ok(
              view(
                saoName = saoName,
                unqualifiedCompanies = unqualifiedCompanies,
                companyCount = unqualifiedCompanies.size,
                dummyDate = dummyDate
              )
            )
          )
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireUploadSubmissionTemplateStageUnlocked).async {
      implicit request =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(CertificateReviewUnqualifiedPage, true))
          _              <- sessionRepository.set(updatedAnswers)
        } yield Redirect(navigator.nextPage(CertificateReviewUnqualifiedPage, NormalMode, request.userAnswers))
    }
}

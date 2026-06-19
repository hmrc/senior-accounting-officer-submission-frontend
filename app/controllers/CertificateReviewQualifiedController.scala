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

package controllers

import controllers.actions.*
import models.NormalMode
import models.upload.*
import navigation.Navigator
import pages.CertificateReviewQualifiedPage
import pages.CertificateSaoFullNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CertificateReviewQualifiedView

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CertificateReviewQualifiedController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    navigator: Navigator,
    sessionRepository: SessionRepository,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    requireUploadSubmissionTemplateStageUnlocked: RequireCertificateUploadSubmissionTemplateUnlockedAction,
    val controllerComponents: MessagesControllerComponents,
    view: CertificateReviewQualifiedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireUploadSubmissionTemplateStageUnlocked) {
      implicit request =>
        {
          // TODO: get the submission data from somewhere more appropriate
          val dummyData = Seq(
            ParsedSubmissionRow(
              notification = NotificationFields(
                companyName = "example company name",
                companyUtr = CompanyUtr("example company utr"),
                companyCrn = Some(CompanyCrn("example company crn")),
                companyType = CompanyType.LTD,
                companyStatus = CompanyStatus.Dormant,
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
                certificateType = Some(CertificateType.Qualified),
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
                valueAddedTax = true,
                paye = false,
                insurancePremiumTax = true,
                stampDutyLandTax = false,
                stampDutyReserveTax = true,
                petroleumRevenueTax = false,
                customsDuties = false,
                exciseDuties = true,
                bankLevy = false,
                certificateType = Some(CertificateType.Qualified),
                additionalInformation = Some("example additional information 2")
              )
            )
          )

          val qualifiedCompanies = dummyData.map(_.toQualifiedCompany)

          request.userAnswers
            .get(CertificateSaoFullNamePage) match {
            case Some(saoName) =>
              Ok(
                view(
                  saoName = saoName,
                  financialYearEnd = LocalDate
                    .of(2024, 12, 31) // TODO: get this from somewhere appropriate
                    .format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                  companyCount = 1,
                  qualifiedCompanies = qualifiedCompanies
                )
              )
            case None => Redirect(routes.JourneyRecoveryController.onPageLoad())
          }
        }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireUploadSubmissionTemplateStageUnlocked).async {
      implicit request =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(CertificateReviewQualifiedPage, true))
          _              <- sessionRepository.set(updatedAnswers)
        } yield Redirect(navigator.nextPage(CertificateReviewQualifiedPage, NormalMode, request.userAnswers))
    }
}

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

package controllers.combined.submitCertificate

import controllers.actions.*
import controllers.routes
import models.upload.*
import models.{CompanyPagination, JourneySection, NormalMode}
import navigation.CombinedNavigator
import pages.certificate.{CertificateReviewUnqualifiedPage, CertificateSaoFullNamePage}
import pages.notification.UploadTemplateTablePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.combined.submitCertificate.CombinedCertificateReviewUnqualifiedView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class CombinedCertificateReviewUnqualifiedController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: CombinedCertificateReviewUnqualifiedView,
    navigator: CombinedNavigator,
    sessionRepository: SessionRepository,
    requireSubmitCertificateStageUnlocked: RequireCombinedSubmitCertificateUnlockedAction
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(page: Int = 0): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireSubmitCertificateStageUnlocked) { implicit request =>
      val userAnswers = request.userAnswers
      (for {
        saoName        <- userAnswers.get(CertificateSaoFullNamePage)
        parsedTemplate <- userAnswers.get(UploadTemplateTablePage)
        totalCompanies       = parsedTemplate.rows.size
        unqualifiedCompanies = parsedTemplate.rows.flatMap(_.toUnqualifiedCompany)
      } yield {
        val allUnqualifiedCompanies = unqualifiedCompanies
        val paginatedData = CompanyPagination(items = allUnqualifiedCompanies, requestedPage = page, pageSize = 10)
        Ok(
          view(
            saoName = saoName,
            unqualifiedCompanies = paginatedData.items,
            companyCount = totalCompanies,
            totalUnqualifiedCompanies = allUnqualifiedCompanies.size,
            currentPage = paginatedData.currentPage,
            pageCount = paginatedData.pageCount
          )
        )
      }).fold(
        Redirect(routes.JourneyRecoveryController.onPageLoad(section = Some(JourneySection.Combined)))
      )(identity)
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireSubmitCertificateStageUnlocked).async {
      implicit request =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(CertificateReviewUnqualifiedPage, true))
          _              <- sessionRepository.set(updatedAnswers)
        } yield Redirect(navigator.nextPage(CertificateReviewUnqualifiedPage, NormalMode, request.userAnswers))
    }
}

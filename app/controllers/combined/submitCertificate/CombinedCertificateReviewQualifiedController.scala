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
import controllers.certificate.routes as certificateRoutes
import controllers.routes
import models.upload.*
import models.{CompanyPagination, JourneySection, NormalMode}
import navigation.CombinedNavigator
import pages.certificate.{CertificateReviewQualifiedPage, CertificateSaoFullNamePage}
import pages.notification.UploadTemplateTablePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.combined.submitCertificate.CombinedCertificateReviewQualifiedView
import views.html.combined.uploadTemplate.CombinedUploadTemplateTableErrorView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class CombinedCertificateReviewQualifiedController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    navigator: CombinedNavigator,
    sessionRepository: SessionRepository,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: CombinedCertificateReviewQualifiedView,
    errorView: CombinedUploadTemplateTableErrorView,
    requireSubmitCertificateStageUnlocked: RequireCombinedSubmitCertificateUnlockedAction
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(page: Int = 0): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireSubmitCertificateStageUnlocked) { implicit request =>
      val userAnswers = request.userAnswers

      userAnswers
        .get(UploadTemplateTablePage)
        .fold(
          Redirect(routes.JourneyRecoveryController.onPageLoad(section = Some(JourneySection.Combined)))
        ) { parsedTemplate =>
          if parsedTemplate.hasErrors then {
            Ok(errorView(parsedTemplate))
          } else {
            userAnswers
              .get(CertificateSaoFullNamePage)
              .fold(
                Redirect(
                  routes.JourneyRecoveryController.onPageLoad(section = Some(JourneySection.Combined))
                )
              ) { saoName =>
                val allQualifiedCompanies =
                  parsedTemplate.rows.flatMap(_.toQualifiedCompany)
                val paginatedData =
                  CompanyPagination(items = allQualifiedCompanies, requestedPage = page, pageSize = 5)
                Ok(
                  view(
                    saoName = saoName,
                    companyCount = parsedTemplate.rows.size,
                    qualifiedCompanies = paginatedData.items,
                    totalQualifiedCompanies = allQualifiedCompanies.size,
                    currentPage = paginatedData.currentPage,
                    pageCount = paginatedData.pageCount
                  )
                )
              }
          }
        }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireSubmitCertificateStageUnlocked).async {
      implicit request =>
        request.userAnswers
          .get(UploadTemplateTablePage)
          .fold(
            Future
              .successful(
                Redirect(
                  routes.JourneyRecoveryController.onPageLoad(section = Some(JourneySection.Certificate))
                )
              )
          ) {
            case tableData if tableData.hasErrors =>
              Future.successful(Redirect(certificateRoutes.CertificateUploadFormController.onPageLoad()))
            case _ =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(CertificateReviewQualifiedPage, true))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(CertificateReviewQualifiedPage, NormalMode, updatedAnswers))
          }
    }
}

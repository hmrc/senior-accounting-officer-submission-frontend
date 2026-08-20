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
import controllers.certificate.routes as certificateRoutes
import controllers.routes
import models.NormalMode
import models.upload.*
import navigation.CertificateNavigator
import pages.certificate.{
  CertificateReviewQualifiedPage,
  CertificateSaoFullNamePage,
  CertificateUploadTemplateTablePage
}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.certificate.{CertificateReviewQualifiedView, CertificateUploadTemplateTableErrorView}

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class CertificateReviewQualifiedController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    navigator: CertificateNavigator,
    sessionRepository: SessionRepository,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    requireUploadSubmissionTemplateStageUnlocked: RequireCertificateUploadSubmissionTemplateUnlockedAction,
    val controllerComponents: MessagesControllerComponents,
    view: CertificateReviewQualifiedView,
    errorView: CertificateUploadTemplateTableErrorView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireUploadSubmissionTemplateStageUnlocked) {
      implicit request =>
        val userAnswers = request.userAnswers

        userAnswers
          .get(CertificateUploadTemplateTablePage)
          .fold(Redirect(routes.JourneyRecoveryController.onPageLoad())) { parsedTemplate =>
            if parsedTemplate.errors.nonEmpty then {
              Ok(errorView(parsedTemplate))
            } else {
              userAnswers
                .get(CertificateSaoFullNamePage)
                .fold(Redirect(routes.JourneyRecoveryController.onPageLoad())) { saoName =>
                  Ok(
                    view(
                      saoName = saoName,
                      companyCount = parsedTemplate.rows.size,
                      qualifiedCompanies = parsedTemplate.rows.flatMap(_.toQualifiedCompany)
                    )
                  )
                }
            }
          }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireUploadSubmissionTemplateStageUnlocked).async {
      implicit request =>
        request.userAnswers
          .get(CertificateUploadTemplateTablePage)
          .fold(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))) {
            case UploadTemplateTableData(_, errors) if errors.nonEmpty =>
              Future.successful(Redirect(certificateRoutes.CertificateUploadFormController.onPageLoad()))
            case _ =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(CertificateReviewQualifiedPage, true))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(CertificateReviewQualifiedPage, NormalMode, updatedAnswers))
          }
    }
}

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
import navigation.Navigator
import pages.{OneSaoSubmitNotificationFullNamePage, UploadTemplateTablePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.UploadTemplateTableView

import javax.inject.Inject

class UploadTemplateTableController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: UploadTemplateTableView,
    navigator: Navigator
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    // TODO turn this into a service to keep the controller clean & update logic to be 1/1 sao or 2/2 sao (the last one entered)
    (
      for {
        tableData <- request.userAnswers.get(UploadTemplateTablePage)
        saoName   <- request.userAnswers.get(OneSaoSubmitNotificationFullNamePage)
      } yield Ok(view(tableData, saoName))
    ).getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    Redirect(navigator.nextPage(UploadTemplateTablePage, NormalMode, request.userAnswers))
  }
}

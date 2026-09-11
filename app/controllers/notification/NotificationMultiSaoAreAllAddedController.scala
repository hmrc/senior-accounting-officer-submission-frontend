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

package controllers.notification

import controllers.actions.*
import forms.notification.NotificationMultiSaoAreAllAddedFormProvider
import models.Mode
import navigation.NotificationNavigator
import pages.notification.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SaoUserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.notification.NotificationMultiSaoAreAllAddedView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject
import models.TransactionMode
import models.UserAnswers
import models.NormalMode
import scala.annotation.tailrec

class NotificationMultiSaoAreAllAddedController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: NotificationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: NotificationMultiSaoAreAllAddedFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: NotificationMultiSaoAreAllAddedView,
    saoUserAnswersService: SaoUserAnswersService
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  def onPageLoad(mode: Mode, saoIndex: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form         = formProvider()
      val preparedForm =
        request.userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex, mode)).fold(form)(form.fill)
      Ok(view(preparedForm, mode, saoIndex))
  }

  def onSubmit(mode: Mode, saoIndex: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider()
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, saoIndex))),
          value =>
            for {
              updatedAnswers <- Future
                .fromTry(request.userAnswers.set(NotificationMultiSaoAreAllAddedPage(saoIndex, mode), value))
              // cleanedAnswers = saoUserAnswersService.cleanupMultiSaoDataAfterIndex(updatedAnswers, saoIndex)
              // _ <- sessionRepository.set(cleanedAnswers)
              _ <- sessionRepository.set(commitTransaction(updatedAnswers, mode, saoIndex))
            } yield Redirect(
              // navigator.nextPage(NotificationMultiSaoAreAllAddedPage(saoIndex, mode), mode, cleanedAnswers)
              navigator
                .nextPage(
                  NotificationMultiSaoAreAllAddedPage(saoIndex, mode),
                  mode,
                  commitTransaction(updatedAnswers, mode, saoIndex)
                )
            )
        )
  }

  def commitTransaction(userAnswers: UserAnswers, mode: Mode, saoIndex: Int): UserAnswers = {
    if mode == TransactionMode && userAnswers.get(
        NotificationMultiSaoAreAllAddedPage(saoIndex, TransactionMode)
      ) == Some(true)
    then {
      def commitOneSao(userAnswers: UserAnswers, saoIndex: Int): UserAnswers = {
        userAnswers.get(NotificationMultiSaoPreviousOfficerNamePage(saoIndex, TransactionMode)).fold(userAnswers) {
          previousOfficerName =>
            userAnswers
              .get(NotificationMultiSaoPreviousOfficerStartDatePage(saoIndex, TransactionMode))
              .fold(userAnswers) { previousOfficerStartDate =>
                userAnswers
                  .get(NotificationMultiSaoPreviousOfficerEndDatePage(saoIndex, TransactionMode))
                  .fold(userAnswers) { previousOfficerEndDate =>
                    userAnswers
                      .get(NotificationMultiSaoAreAllAddedPage(saoIndex, TransactionMode))
                      .fold(userAnswers) { areAllAdded =>
                        userAnswers
                          .set(
                            NotificationMultiSaoPreviousOfficerNamePage(saoIndex, NormalMode),
                            previousOfficerName
                          )
                          .get
                          .set(
                            NotificationMultiSaoPreviousOfficerStartDatePage(saoIndex, NormalMode),
                            previousOfficerStartDate
                          )
                          .get
                          .set(
                            NotificationMultiSaoPreviousOfficerEndDatePage(saoIndex, NormalMode),
                            previousOfficerEndDate
                          )
                          .get
                          .set(
                            NotificationMultiSaoAreAllAddedPage(saoIndex, NormalMode),
                            areAllAdded
                          )
                          .get
                      }
                  }
              }
        }
      }

      @tailrec
      def recur(userAnswers: UserAnswers, saoIndex: Int): UserAnswers = {
        if userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex, TransactionMode)) == Some(true) then
          commitOneSao(userAnswers, saoIndex)
        else
          recur(
            commitOneSao(userAnswers, saoIndex),
            saoIndex + 1
          )
      }
      userAnswers
        .get(NotificationMoreThanOneSaoPage(TransactionMode))
        .fold(userAnswers) { moreThanOne =>
          userAnswers.get(NotificationMultiSaoLastOfficerNamePage(TransactionMode)).fold(userAnswers) {
            lastOfficerName =>
              userAnswers.get(NotificationMultiSaoLastOfficerStartDatePage(TransactionMode)).fold(userAnswers) {
                lastOfficerStartDate =>
                  recur(
                    userAnswers
                      .set(NotificationMoreThanOneSaoPage(NormalMode), moreThanOne)
                      .get
                      .set(NotificationMultiSaoLastOfficerNamePage(NormalMode), lastOfficerName)
                      .get
                      .set(
                        NotificationMultiSaoLastOfficerStartDatePage(NormalMode),
                        lastOfficerStartDate
                      )
                      .get,
                    0
                  )
              }
          }
        }

    } else {
      userAnswers
    }
  }
}

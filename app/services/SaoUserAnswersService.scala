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

import models.Area.COMMITTED_PATH
import models.Area.TRANSACTION_PATH
import models.NormalMode
import models.TransactionMode
import models.UserAnswers
import pages.Page.NOTIFICATION_PATH
import pages.notification.*
import play.api.Logging
import play.api.libs.json.*
import play.api.libs.json.Reads.*

import scala.annotation.tailrec

import javax.inject.Inject

class SaoUserAnswersService extends Logging @Inject {

  val singleSaoNameKey: String         = NotificationSingleSaoOfficerNamePage(NormalMode).toString
  val multiSaoLastNameKey: String      = NotificationMultiSaoLastOfficerNamePage(NormalMode).toString
  val multiSaoLastStartDateKey: String = NotificationMultiSaoLastOfficerStartDatePage(NormalMode).toString
  val multiSaoNameKey: String          = NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode).key
  val multiSaoStartDateKey: String     = NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode).key
  val multiSaoEndDateKey: String       = NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode).key
  val multiSaoAddedAllKey: String      = NotificationMultiSaoAreAllAddedPage(0, NormalMode).key

  def jacobPrint[A](a: A): A = {
    println(a)
    a
  }

  def sanitiseUserAnswers(userAnswers: UserAnswers): UserAnswers = {
    userAnswers.get(NotificationMoreThanOneSaoPage(NormalMode)) match {
      case Some(true) =>
        userAnswers
          .clearTransactionArea()
          .clearCommittedAreaSingleSao()
          .sanitiseCommittedAreaMultiSao()
          .copyCommittedAreaToTransactionArea()
      case Some(false) =>
        userAnswers
          .clearTransactionArea()
          .clearCommittedAreaMultiSao()
          .copyCommittedAreaToTransactionArea()
      case None => ???
    }
  }

  extension (userAnswers: UserAnswers) {
    def copyCommittedAreaToTransactionArea(): UserAnswers = {
      userAnswers.transformUserAnswers(
        (__ \ NOTIFICATION_PATH).json.update(
          __.read[JsObject].map { o =>
            Json.obj(TRANSACTION_PATH -> userAnswers.data(NOTIFICATION_PATH)(COMMITTED_PATH))
          }
        )
      )
    }

    def sanitiseCommittedAreaMultiSao(): UserAnswers = {
      val finalIndex    = finalCompleteSaoIndex(userAnswers)
      val takeFromArray = of[JsArray].map { case JsArray(contents) => JsArray(contents.take(finalIndex + 1)) }
      userAnswers
        .transformUserAnswers(
          (__ \ NOTIFICATION_PATH \ COMMITTED_PATH).json
            .update(
              (__ \ multiSaoNameKey).json.update(takeFromArray) andThen
                (__ \ multiSaoStartDateKey).json.update(takeFromArray) andThen
                (__ \ multiSaoEndDateKey).json.update(takeFromArray) andThen
                (__ \ multiSaoAddedAllKey).json.update(takeFromArray) andThen
                (__ \ multiSaoAddedAllKey).json.update(of[JsArray].map { case JsArray(contents) =>
                  JsArray(contents.dropRight(1) :+ JsTrue)
                })
            )
        )
    }

    def clearCommittedAreaSingleSao(): UserAnswers = {
      userAnswers.transformUserAnswers((__ \ NOTIFICATION_PATH \ COMMITTED_PATH \ singleSaoNameKey).json.prune)
    }

    def clearCommittedAreaMultiSao(): UserAnswers = {
      userAnswers.transformUserAnswers(
        (__ \ NOTIFICATION_PATH \ COMMITTED_PATH \ multiSaoLastNameKey).json.prune andThen
          (__ \ NOTIFICATION_PATH \ COMMITTED_PATH \ multiSaoLastStartDateKey).json.prune andThen
          (__ \ NOTIFICATION_PATH \ COMMITTED_PATH \ multiSaoNameKey).json.prune andThen
          (__ \ NOTIFICATION_PATH \ COMMITTED_PATH \ multiSaoStartDateKey).json.prune andThen
          (__ \ NOTIFICATION_PATH \ COMMITTED_PATH \ multiSaoEndDateKey).json.prune andThen
          (__ \ NOTIFICATION_PATH \ COMMITTED_PATH \ multiSaoAddedAllKey).json.prune
      )
    }

    def clearTransactionArea(): UserAnswers = {
      userAnswers.transformUserAnswers((__ \ NOTIFICATION_PATH \ TRANSACTION_PATH).json.prune)
    }

    def transformUserAnswers(transformer: Reads[JsObject]): UserAnswers = {
      userAnswers.data.transform(transformer) match {
        case JsError(error) => {
          logger.error("Json transformation error: " + error)
          ???
        }
        case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
      }
    }

  }

  def removeShadow(userAnswers: UserAnswers): UserAnswers = {
    val transformer = (__ \ NOTIFICATION_PATH \ TRANSACTION_PATH).json.prune
    userAnswers.data.transform(transformer) match {
      case JsError(_)                => ???
      case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
    }
  }

  def copyActualToShadow(userAnswers: UserAnswers): UserAnswers = {
    val transformer =
      (__ \ NOTIFICATION_PATH \ TRANSACTION_PATH).json
        .copyFrom((__ \ NOTIFICATION_PATH \ COMMITTED_PATH).json.pick) andThen (__).json
        .update((__ \ NOTIFICATION_PATH \ COMMITTED_PATH).json.pick)

    userAnswers.data.transform(transformer) match {
      case JsError(_) =>
        ???
      case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
    }
  }

  def copyShadowToActual(userAnswers: UserAnswers): UserAnswers = {
    val transformer =
      (__ \ NOTIFICATION_PATH \ COMMITTED_PATH).json
        .copyFrom((__ \ NOTIFICATION_PATH \ TRANSACTION_PATH).json.pick) andThen (__).json
        .update((__ \ NOTIFICATION_PATH \ TRANSACTION_PATH).json.pick)

    userAnswers.data.transform(transformer) match {
      case JsError(_)                => ???
      case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
    }
  }

  private def finalCompleteSaoIndex(userAnswers: UserAnswers): Int = {
    @tailrec
    def recur(saoIndex: Int): Int = {
      if userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex, NormalMode)) == Some(
          true
        ) || !isSaoAtIndexCompleted(
          userAnswers,
          saoIndex + 1
        )
      then {
        saoIndex
      } else {
        recur(saoIndex + 1)
      }
    }
    recur(0)
  }

  private def isSaoAtIndexCompleted(userAnswers: UserAnswers, saoIndex: Int): Boolean = {
    userAnswers.get(NotificationMultiSaoPreviousOfficerNamePage(saoIndex, NormalMode)).nonEmpty &&
    userAnswers.get(NotificationMultiSaoPreviousOfficerStartDatePage(saoIndex, NormalMode)).nonEmpty &&
    userAnswers.get(NotificationMultiSaoPreviousOfficerEndDatePage(saoIndex, NormalMode)).nonEmpty &&
    userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex, NormalMode)).nonEmpty
  }

  def commitTransaction(userAnswers: UserAnswers): UserAnswers = {
    if userAnswers.get(NotificationMoreThanOneSaoPage(TransactionMode)) == Some(true) then {
      commitMultiSaoTransaction(userAnswers)
    } else {
      commitSingleSaoTransaction(userAnswers)
    }
  }

  private def commitSingleSaoTransaction(userAnswers: UserAnswers): UserAnswers = {
    userAnswers.get(NotificationMoreThanOneSaoPage(TransactionMode)).fold(userAnswers) { moreThanOne =>
      userAnswers.get(NotificationSingleSaoOfficerNamePage(TransactionMode)).fold(userAnswers) { name =>
        userAnswers
          .set(NotificationMoreThanOneSaoPage(NormalMode), moreThanOne)
          .get
          .set(NotificationSingleSaoOfficerNamePage(NormalMode), name)
          .get
      }
    }
  }

  private def commitMultiSaoTransaction(userAnswers: UserAnswers): UserAnswers = {
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
      if userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex, TransactionMode)) == Some(true) then {
        commitOneSao(userAnswers, saoIndex)
      } else {
        recur(
          commitOneSao(userAnswers, saoIndex),
          saoIndex + 1
        )
      }
    }
    userAnswers
      .get(NotificationMoreThanOneSaoPage(TransactionMode))
      .fold(userAnswers) { moreThanOne =>
        userAnswers.get(NotificationMultiSaoLastOfficerNamePage(TransactionMode)).fold(userAnswers) { lastOfficerName =>
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
  }
}

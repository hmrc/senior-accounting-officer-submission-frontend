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

import models.UserAnswers
import pages.notification.*
import play.api.libs.json.*
import play.api.libs.json.Reads.*

import scala.annotation.tailrec

import javax.inject.Inject

import models.NormalMode
import play.api.Logging

class SaoUserAnswersService extends Logging @Inject {

  val singleSaoNameKey             = NotificationSingleSaoOfficerNamePage(NormalMode).toString
  val multiSaoLastNameKey          = NotificationMultiSaoLastOfficerNamePage(NormalMode).toString
  val multiSaoLastStartDateKey     = NotificationMultiSaoLastOfficerStartDatePage(NormalMode).toString
  val multiSaoNameKey: String      = NotificationMultiSaoPreviousOfficerNamePage(0, NormalMode).key
  val multiSaoStartDateKey: String = NotificationMultiSaoPreviousOfficerStartDatePage(0, NormalMode).key
  val multiSaoEndDateKey: String   = NotificationMultiSaoPreviousOfficerEndDatePage(0, NormalMode).key
  val multiSaoAddedAllKey: String  = NotificationMultiSaoAreAllAddedPage(0, NormalMode).key

  // TODO: remove
  def cleanupMultiSaoDataAfterIndex(userAnswers: UserAnswers, saoIndex: Int): UserAnswers = {
    val userAnsweredYes = userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex, NormalMode)) == Some(true)

    if userAnsweredYes then {

      val takeFromArray = of[JsArray].map { case JsArray(contents) => JsArray(contents.take(saoIndex + 1)) }

      val transformer = (__ \ "notification").json.update(
        (__ \ multiSaoNameKey).json.update(takeFromArray) andThen
          (__ \ multiSaoStartDateKey).json.update(takeFromArray) andThen
          (__ \ multiSaoEndDateKey).json.update(takeFromArray) andThen
          (__ \ multiSaoAddedAllKey).json.update(takeFromArray)
      )

      userAnswers.data.transform(transformer) match {
        case JsError(_)                => ???
        case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
      }
    } else {
      userAnswers
    }
  }

  def jacobPrint[A](a: A): A = {
    println(a)
    a
  }

  def sanitiseUserAnswers(userAnswers: UserAnswers): UserAnswers = {
    userAnswers.get(NotificationMoreThanOneSaoPage(NormalMode)) match {
      case Some(true) =>
        userAnswers
          .clearShadowRealm()
          .clearActualRealmSingleSao()
          .sanitiseActualRealmMultiSao()
          .copyActualRealmToShadowRealm()
      case Some(false) =>
        userAnswers
          .clearShadowRealm()
          .clearActualRealmMultiSao()
          .copyActualRealmToShadowRealm()
      case None => ???
    }
  }

  extension (userAnswers: UserAnswers) {
    def copyActualRealmToShadowRealm(): UserAnswers = {
      userAnswers.transformUserAnswers(
        (__ \ "notification").json.update(
          __.read[JsObject].map { o => Json.obj("Shadow" -> userAnswers.data("notification")("Actual")) }
        )
      )
    }

    def sanitiseActualRealmMultiSao(): UserAnswers = {
      val finalIndex    = finalCompleteSaoIndex(userAnswers)
      val takeFromArray = of[JsArray].map { case JsArray(contents) => JsArray(contents.take(finalIndex + 1)) }
      userAnswers
        .transformUserAnswers(
          (__ \ "notification" \ "Actual").json
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

    def clearActualRealmSingleSao(): UserAnswers = {
      userAnswers.transformUserAnswers((__ \ "notification" \ "Actual" \ singleSaoNameKey).json.prune)
    }

    def clearActualRealmMultiSao(): UserAnswers = {
      userAnswers.transformUserAnswers(
        (__ \ "notification" \ "Actual" \ multiSaoLastNameKey).json.prune andThen
          (__ \ "notification" \ "Actual" \ multiSaoLastStartDateKey).json.prune andThen
          (__ \ "notification" \ "Actual" \ multiSaoNameKey).json.prune andThen
          (__ \ "notification" \ "Actual" \ multiSaoStartDateKey).json.prune andThen
          (__ \ "notification" \ "Actual" \ multiSaoEndDateKey).json.prune andThen
          (__ \ "notification" \ "Actual" \ multiSaoAddedAllKey).json.prune
      )
    }

    def clearShadowRealm(): UserAnswers = {
      userAnswers.transformUserAnswers((__ \ "notification" \ "Shadow").json.prune)
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
    val transformer = (__ \ "notification" \ "Shadow").json.prune
    userAnswers.data.transform(transformer) match {
      case JsError(_)                => ???
      case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
    }
  }

  def copyActualToShadow(userAnswers: UserAnswers): UserAnswers = {
    val transformer =
      (__ \ "notification" \ "Shadow").json.copyFrom((__ \ "notification" \ "Actual").json.pick) andThen (__).json
        .update((__ \ "notification" \ "Actual").json.pick)

    userAnswers.data.transform(transformer) match {
      case JsError(_) =>
        ???
      case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
    }
  }

  def copyShadowToActual(userAnswers: UserAnswers): UserAnswers = {
    val transformer =
      (__ \ "notification" \ "Actual").json.copyFrom((__ \ "notification" \ "Shadow").json.pick) andThen (__).json
        .update((__ \ "notification" \ "Shadow").json.pick)

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

  /** Remove from useranswers data which concerns the other SAO flow.
    *
    * If the user is providing answers for a single SAO, delete any answers they might have provided for multiple SAOs,
    * and vice versa.
    */
  def removeOtherSaoJourneyData(userAnswers: UserAnswers): UserAnswers = {
    userAnswers.get(NotificationMoreThanOneSaoPage(NormalMode)) match {
      case Some(true) => {
        val transformer = (__ \ "notification" \ singleSaoNameKey).json.prune

        userAnswers.data.transform(transformer) match {
          case JsError(errors)           => ???
          case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
        }
      }
      case Some(false) => pruneMultiSaoAnswers(userAnswers)
      case None        => ???
    }
  }

  private def pruneMultiSaoAnswers(userAnswers: UserAnswers): UserAnswers = {
    val transformer =
      (__ \ "notification" \ "Shadow").json.prune andThen
        (__ \ "notification" \ "Actual" \ multiSaoLastNameKey).json.prune andThen
        (__ \ "notification" \ "Actual" \ multiSaoLastStartDateKey).json.prune andThen
        (__ \ "notification" \ "Actual" \ multiSaoNameKey).json.prune andThen
        (__ \ "notification" \ "Actual" \ multiSaoStartDateKey).json.prune andThen
        (__ \ "notification" \ "Actual" \ multiSaoEndDateKey).json.prune andThen
        (__ \ "notification" \ "Actual" \ multiSaoAddedAllKey).json.prune andThen
        (__ \ "notification").read[JsObject].map { o =>
          o ++ Json.obj("Shadow" -> (userAnswers.data("notification")("Actual")))
        }

    userAnswers.data.transform(transformer) match {
      case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
      case JsError(_)                => ???
    }
  }
}

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
import play.api.libs.json.JsArray
import play.api.libs.json.Reads.*

import scala.annotation.tailrec

import javax.inject.Inject

import models.NormalMode

class SaoUserAnswersService @Inject {

  val singleSaoNameKey             = NotificationSingleSaoOfficerNamePage(NormalMode).toString
  val multiSaoLastNameKey          = NotificationMultiSaoLastOfficerNamePage.toString
  val multiSaoLastStartDateKey     = NotificationMultiSaoLastOfficerStartDatePage.toString
  val multiSaoNameKey: String      = NotificationMultiSaoPreviousOfficerNamePage(0).key
  val multiSaoStartDateKey: String = NotificationMultiSaoPreviousOfficerStartDatePage(0).key
  val multiSaoEndDateKey: String   = NotificationMultiSaoPreviousOfficerEndDatePage(0).key
  val multiSaoAddedAllKey: String  = NotificationMultiSaoAreAllAddedPage(0).key

  def cleanupMultiSaoDataAfterIndex(userAnswers: UserAnswers, saoIndex: Int): UserAnswers = {
    val userAnsweredYes = userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex)) == Some(true)

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

  def sanitiseUserAnswers(userAnswers: UserAnswers): UserAnswers = {
    userAnswers.get(NotificationMoreThanOneSaoPage(NormalMode)) match {
      case Some(true) => {
        val finalIndex = finalCompleteSaoIndex(userAnswers)

        val takeFromArray = of[JsArray].map { case JsArray(contents) => JsArray(contents.take(finalIndex + 1)) }

        val transformer =
          (__ \ "notification" \ singleSaoNameKey).json.prune andThen
            (__ \ "notification").json.update(
              (__ \ multiSaoNameKey).json.update(takeFromArray) andThen
                (__ \ multiSaoStartDateKey).json.update(takeFromArray) andThen
                (__ \ multiSaoEndDateKey).json.update(takeFromArray) andThen
                (__ \ multiSaoAddedAllKey).json.update(takeFromArray) andThen
                (__ \ multiSaoAddedAllKey).json.update(of[JsArray].map { case JsArray(contents) =>
                  JsArray(contents.dropRight(1) :+ JsTrue)
                })
            )

        userAnswers.data.transform(transformer) match {
          case JsError(_)                => ???
          case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
        }
      }
      case Some(false) => pruneMultiSaoAnswers(userAnswers)
      case None        => ???
    }
  }

  private def finalCompleteSaoIndex(userAnswers: UserAnswers): Int = {
    @tailrec
    def recur(saoIndex: Int): Int = {
      if userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex)) == Some(true) || !isSaoAtIndexCompleted(
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
    userAnswers.get(NotificationMultiSaoPreviousOfficerNamePage(saoIndex)).nonEmpty &&
    userAnswers.get(NotificationMultiSaoPreviousOfficerStartDatePage(saoIndex)).nonEmpty &&
    userAnswers.get(NotificationMultiSaoPreviousOfficerEndDatePage(saoIndex)).nonEmpty &&
    userAnswers.get(NotificationMultiSaoAreAllAddedPage(saoIndex)).nonEmpty
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
      (__ \ "notification" \ multiSaoLastNameKey).json.prune andThen
        (__ \ "notification" \ multiSaoLastStartDateKey).json.prune andThen
        (__ \ "notification" \ multiSaoNameKey).json.prune andThen
        (__ \ "notification" \ multiSaoStartDateKey).json.prune andThen
        (__ \ "notification" \ multiSaoEndDateKey).json.prune andThen
        (__ \ "notification" \ multiSaoAddedAllKey).json.prune

    userAnswers.data.transform(transformer) match {
      case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
      case JsError(_)                => ???
    }
  }
}

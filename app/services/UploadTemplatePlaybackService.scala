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
import models.upload.UploadTemplateTableData
import pages.*
import pages.notification.*

import javax.inject.{Inject, Singleton}

object UploadTemplatePlaybackService {
  final case class Playback(tableData: UploadTemplateTableData, saoName: String)
}

@Singleton
class UploadTemplatePlaybackService @Inject() () {

  import UploadTemplatePlaybackService.Playback

  def getPlayback(userAnswers: UserAnswers): Option[Playback] =
    for {
      tableData <- userAnswers.get(UploadTemplateTablePage)
      saoName   <- getSaoName(userAnswers)
    } yield Playback(tableData, saoName)

  private def getSaoName(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(NotificationMoreThanOneSaoPage).flatMap {
      case true  => userAnswers.get(NotificationMultiSaoLastOfficerNamePage)
      case false => userAnswers.get(NotificationSingleSaoOfficerNamePage)
    }
}

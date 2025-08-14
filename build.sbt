import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "senior-accounting-officer-submission-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.6"

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    name := appName,
    RoutesKeys.routesImport ++= Seq(
      "models.*",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat.*",
      "uk.gov.hmrc.govukfrontend.views.html.components.*",
      "uk.gov.hmrc.hmrcfrontend.views.html.components.*",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers.*",
      "uk.gov.hmrc.hmrcfrontend.views.config.*",
      "controllers.routes.*",
      "views.ViewUtils.*",
      "viewmodels.govuk.all.*",
      "viewmodels.*"
    ),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged          := true,
    pipelineStages           := Seq(digest, gzip),
    Assets / pipelineStages  := Seq(concat),
    PlayKeys.playDefaultPort := 10058
  )
  .settings(CodeCoverageSettings.settings *)

lazy val it =
  (project in file("it"))
    .enablePlugins(PlayScala)
    .dependsOn(microservice % "test->test")

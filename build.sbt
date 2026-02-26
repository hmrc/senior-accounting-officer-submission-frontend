import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "senior-accounting-officer-submission-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.6"

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(inConfig(Test)(testSettings)*)
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
    scalacOptions ++= Seq(
      "-feature",
      "-no-indent",
      "-Wconf:" + Seq(
        "cat=deprecation:w",
        "cat=feature:w",
        "src=target/.*:s",
        "src=test/.*&id=E175:s",
        "src=test/.*&id=E176:s"
      ).mkString(",")
    ),
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    libraryDependencies ++= AppDependencies(),
    retrieveManaged          := true,
    pipelineStages           := Seq(digest, gzip),
    Assets / pipelineStages  := Seq(concat),
    PlayKeys.playDefaultPort := 10058
  )
  .settings(CodeCoverageSettings.settings*)
  .settings(scalafixSettings*)

lazy val testSettings: Seq[Def.Setting[?]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val it =
  (project in file("it"))
    .enablePlugins(PlayScala)
    .dependsOn(microservice % "test->test")

val scalafixSettings: Seq[Setting[?]] = Seq(
  semanticdbEnabled := true, // enable SemanticDB
  scalacOptions += {
    "-Wall"
  }
)

addCommandAlias("lint", "scalafixAll;scalafmtAll")

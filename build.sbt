import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "senior-accounting-officer-submission-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.6"

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(inConfig(Test)(testSettings) *)
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
      "controllers.routes.*"
    ),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged          := true,
    pipelineStages           := Seq(digest, gzip),
    Assets / pipelineStages  := Seq(concat),
    PlayKeys.playDefaultPort := 10058,
    Compile / compile        := (Compile / compile dependsOn compileScalastyle).value
  )
  .settings(CodeCoverageSettings.settings *)

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val it =
  (project in file("it"))
    .enablePlugins(PlayScala)
    .dependsOn(microservice % "test->test")

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
compileScalastyle := scalastyle.in(Compile).toTask("").value

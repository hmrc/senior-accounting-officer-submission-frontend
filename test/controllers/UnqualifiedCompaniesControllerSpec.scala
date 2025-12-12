package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.UnqualifiedCompaniesView

class UnqualifiedCompaniesControllerSpec extends SpecBase {

  "UnqualifiedCompanies Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.UnqualifiedCompaniesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnqualifiedCompaniesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(using request, messages(application)).toString
      }
    }
  }
}

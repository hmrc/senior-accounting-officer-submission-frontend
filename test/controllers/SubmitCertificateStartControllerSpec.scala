package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.SubmitCertificateStartView

class SubmitCertificateStartControllerSpec extends SpecBase {

  "SubmitCertificateStart Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmitCertificateStartController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmitCertificateStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(using request, messages(application)).toString
      }
    }
  }
}

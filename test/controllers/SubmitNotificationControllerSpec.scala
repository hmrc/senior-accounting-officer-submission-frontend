package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.SubmitNotificationView

class SubmitNotificationControllerSpec extends SpecBase {

  "SubmitNotification Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmitNotificationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmitNotificationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(using request, messages(application)).toString
      }
    }
  }
}

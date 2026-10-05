package views

class UnexpectedErrorViewSpec extends ViewSpecBase[UnexpectedErrorView] {
  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "UnexpectedErrorView" - {
    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = false,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = true
    )
    doc.getMainContent
      .select("p a")
      .get(0)
      .createTestWithLink(
        linkText = pageDownload,
        destinationUrl = pageDownloadUrl
      )
    doc.createTestsWithOrWithoutError(hasError = true)
  }
}

object SystemErrorViewSpec {
  val pageHeading = "Sorry, there is a problem with the service"
  val pageTitle = "Sorry, there is a problem with the service"
  val pageParagraphs = Seq("Try again later.", )
}

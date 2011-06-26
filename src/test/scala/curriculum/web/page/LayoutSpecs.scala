package curriculum.web.page

import org.specs.Specification
import org.slf4j.LoggerFactory

class LayoutSpecs extends Specification {

  val _logger = LoggerFactory.getLogger(classOf[LayoutSpecs])

  "Layout" should {
    "merge a simple page easily" in {
      val layout = new Layout {}
      val pageData =
        <html>
          <head>
              <link rel="stylesheet" type="text/css" href="mypage.css"/>
          </head>
          <body onload="do_something_onload()">
            <h1>Hello!!</h1>
            <div class="content">
              <input type="text" value="Sherlock"/>
              <p>
                Elementary my dear Watson!
              </p>
            </div>
          </body>
        </html>

      val html = layout.render(pageData)
      _logger.debug(html.toString)
    }
  }
}
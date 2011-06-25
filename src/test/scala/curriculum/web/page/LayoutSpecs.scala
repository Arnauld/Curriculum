package curriculum.web.page

import org.specs.Specification

class LayoutSpecs extends Specification {

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
      println(html.toString)
    }
  }
}
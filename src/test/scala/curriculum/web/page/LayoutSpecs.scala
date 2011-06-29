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
      html must equalIgnoreSpace(
        <html>
          <head>
            <link rel="stylesheet" type="text/css" href="resources/jquery/smoothness/jquery-ui-1.8.11.custom.css"></link>
            <link rel="stylesheet" type="text/css" href="resources/curriculum.css"></link>
            <script src="resources/jquery/jquery-1.5.1.min.js" type="text/javascript"></script>
              <link rel="stylesheet" type="text/css" href="mypage.css"/>
          </head>
          <body>
            <!-- Retrieve page's body content except scrip -->
            <h1>Hello!!</h1>
            <div class="content">
                <input type="text" value="Sherlock"/>
              <p>
                Elementary my dear Watson!
              </p>
            </div>
            <!-- Load js once the page is loaded to have at least something to read in the
             meanwhile, otherwise put script in the head part to bypass this beahvior -->
            <script src="resources/jquery/jquery-ui-1.8.11.custom.min.js" type="text/javascript"></script>
            <script src="resources/curriculum.js" type="text/javascript"></script>
          </body>
        </html>)
    }
  }
}
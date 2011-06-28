package curriculum.web.page

import org.specs.Specification
import xml.NodeSeq

class XmlSpecs extends Specification {

  val pageData =
    <html>
      <head>
          <link rel="stylesheet" type="text/css" href="mypage.css"/>
      </head>
      <body onload="do_something_onload()">
        <div class="header">
          <h1>Hello!!</h1>
        </div>
        <div class="content">
            <input type="text" value="Sherlock"/>
          <p>
            Elementary my dear Watson!
          </p>
        </div>
        <div class="header">
          <b>Copyright</b>
        </div>
      </body>
    </html>

  "XML" should {
    "provide XPath support for simple query" in {
      val selected = pageData \ "head" \ "_"
      selected must_== NodeSeq.Empty ++
          <link rel="stylesheet" type="text/css" href="mypage.css"/>
    }

    "provide XPath support for a more complex query" in {
      val divs = (pageData \ "body" \ "div").filter(_.attribute("class") match {
        case None => false
        case Some(n) => n.text == "content"
      })
      val selected = ( divs \ "_" )
      selected must_== NodeSeq.Empty ++
          <input type="text" value="Sherlock"/> ++
          <p>
            Elementary my dear Watson!
          </p>
    }
  }
}
package curriculum.web

import org.scalatra._
import curriculum.util.Strings
import org.slf4j.LoggerFactory

class CurriculumFilter extends ScalatraFilter with ResourceSupport {

  val log = LoggerFactory.getLogger(classOf[CurriculumFilter])

  get("/test") {
    log.debug("Test called!")
    <h1>Test resource</h1>
  }
  
/*
  post("/msgs") {
    val builder = MongoDBObject.newBuilder
    params.get("body").foreach(msg => {
      builder += ("body" -> msg)
      mongo += builder.result
    })
    redirect("/msgs")
  }
*/
/*
  get("/arnauld") {
    <body>
      <ul>
        {for (msg <- mongo) yield <li>{msg.get("body")}</li>}
      </ul>
      <form method="POST" action="/msgs">
        <input type="text" name="body"/>
        <input type="submit"/>
      </form>
    </body>  
  }
*/

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  get("/stylesheets/:what") {
    val what = params("what")
    getResource("/WEB-INF/stylesheets/" + what, "text/css")
  }

  get("/images/*") {
    val what = params("splat")
    getResource("/WEB-INF/images/" + what, "image/" + Strings.extensionOf(what))
  }

  get("/javascripts/:what") {
    val what = params("what")
    getResource("/WEB-INF/javascripts/" + what, "text/javascript")
  }

}
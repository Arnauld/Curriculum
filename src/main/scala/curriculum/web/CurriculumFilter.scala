package curriculum.web

import org.scalatra._
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import curriculum.util.{Bytes, Strings}
import page.Layout
import curriculum.eav.service.{InstanceLoader, InstanceService, ModelLoader, EntityService}
import curriculum.domain.{CurriculumVitaeInstances, CurriculumVitaeModels}
import curriculum.domain.web.CurriculumVitaePage

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

  get("/arnauld") {
    val logRef = log
    val entityService = new EntityService {}
    val modelReader = new ModelLoader {
      def getEntityService = entityService

      def log = LoggerFactory.getLogger("curriculum.eav.service.ModelLoader")
    }
    val instanceService = new InstanceService {}
    val instanceReader = new InstanceLoader {
      def getInstanceService = instanceService

      def getEntityService = entityService

      def log = LoggerFactory.getLogger("curriculum.eav.service.InstanceLoader")
    }
    modelReader.load(CurriculumVitaeModels.Models)
    val male = instanceReader.loadInstance(CurriculumVitaeInstances.GenreMale)
    val female = instanceReader.loadInstance(CurriculumVitaeInstances.GenreFemale)
    instanceService.register(male)
    instanceService.register(female)

    val instance = instanceReader.loadInstance(CurriculumVitaeInstances.Arnauld)

    /*
     * generate page
     */
    val page = new CurriculumVitaePage(instance)
    val layout = new Layout {}
    layout.render(page.content)
  }


  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  var skipResourceManagement = true
  get("/resources/*") {
    skipResourceManagement match {
      case true => pass()
      case _ =>
        val what = params("splat")
        val ctype = Strings.extensionOf(what).toLowerCase match {
          case "css" => Some("text/css")
          case "js" => Some("text/javascript")
          case "png" => Some("image/png")
          case _ => None
        }
        ctype match {
          case None =>
            log.warn("Invalid resource <" + what + "> queried")
            response.setStatus(HttpURLConnection.HTTP_NOT_ACCEPTABLE)
            Bytes.EMPTY
          case Some(ctype) =>
            getResource("/resources/" + what, ctype)
        }
    }
  }

}
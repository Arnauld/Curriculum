package curriculum.web

import org.scalatra._
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import curriculum.util.{Bytes, Strings}
import page.Layout
import curriculum.eav.service.{InstanceLoader, InstanceService, ModelLoader, EntityService}
import curriculum.domain.{CurriculumVitaeInstances, CurriculumVitaeModels}
import curriculum.domain.web.CurriculumVitaePage
import xml.Node

class CurriculumFilter extends ScalatraFilter with ResourceSupport {

  val log = LoggerFactory.getLogger(classOf[CurriculumFilter])

  get("/test") {
    log.debug("Test called!")
    <h1>Test resource</h1>
  }

  get("/arnauld") {
    val entityService = new EntityService {}
    val modelReader = new ModelLoader {
      def getEntityService = entityService
    }
    val instanceService = new InstanceService {}
    val instanceReader = new InstanceLoader {
      def getInstanceService = instanceService
      def getEntityService = entityService
    }
    modelReader.load(CurriculumVitaeModels.Models)

    import CurriculumVitaeInstances._
    List(GenreMale,GenreFemale).foreach({n:Node =>
      val instance = instanceReader.loadInstance(n)
      instanceService.register(instance)
    })

    val instance = instanceReader.loadInstance(Arnauld)

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
          case Some(contentType) =>
            getResource("/resources/" + what, contentType)
        }
    }
  }

}
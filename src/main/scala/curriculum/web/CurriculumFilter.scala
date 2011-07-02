package curriculum.web

import org.scalatra._
import org.slf4j.LoggerFactory
import curriculum.util.{Bytes, Strings}
import page.Layout
import curriculum.eav.service.{InstanceLoader, InstanceService, ModelLoader, EntityService}
import curriculum.domain.{CurriculumVitaeInstances, CurriculumVitaeModels}
import curriculum.domain.web.CurriculumVitaePage
import xml.Node
import curriculum.cluster.page.ClusterAdminPage
import java.net.HttpURLConnection
import java.util.Date
import curriculum.cluster.{ClusterNode, ClusterService}
import curriculum.task.TaskService

class CurriculumFilter extends ScalatraFilter with ResourceSupport {

  val log = LoggerFactory.getLogger(classOf[CurriculumFilter])

  /*
   * TODO ioc
   */
  val entityService = new EntityService {}
  val modelReader = new ModelLoader {
    def getEntityService = entityService
  }
  val instanceService = new InstanceService {}
  val instanceReader = new InstanceLoader {
    def getInstanceService = instanceService

    def getEntityService = entityService
  }
  val taskService = new TaskService {}
  val clusterService = new ClusterService {}

  /**
   * test
   */
  get("/test") {
    log.debug("Test called!")
    <h1>Server up! {new Date().formatted("yyyy/MM/dd HH:mm:ss-SSS Z")}</h1>
  }


  /**
   * Start a node
   */
  post("/cluster/start") {
    log.debug("Cluster node start: {}", params)
    val nodeName = params("start-node-name")
    val nodePort = params("start-node-port")
    val node = ClusterNode(nodeName, nodePort.toInt, Map[String,Any]())
    val task = taskService.spawn(clusterService.startNodeRunnable(node))
    response.setContentType("text/json")
    """{
       task_id:%d
    }""".format(task.taskId)
  }

  /**
   * cluster admin page
   */
  get("/cluster/admin") {
    val clusterPage = new ClusterAdminPage
    val layout = new Layout {}
    layout.render(clusterPage.content)
  }

  /**
   * cv ;)
   */
  get("/arnauld") {
    modelReader.load(CurriculumVitaeModels.Models)

    import CurriculumVitaeInstances._
    List(GenreMale, GenreFemale).foreach({
      n: Node =>
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
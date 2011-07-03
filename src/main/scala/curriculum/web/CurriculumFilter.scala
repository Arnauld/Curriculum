package curriculum.web

import org.scalatra._
import org.slf4j.LoggerFactory
import page.Layout
import curriculum.domain.{CurriculumVitaeInstances, CurriculumVitaeModels}
import curriculum.domain.web.CurriculumVitaePage
import xml.Node
import curriculum.cluster.page.ClusterAdminPage
import java.net.HttpURLConnection
import curriculum.util.{Bytes, Strings}
import java.util.{Locale, Date}
import curriculum.task.TaskMessage
import curriculum.cluster.{ClusterMessage, ClusterNode}

class CurriculumFilter extends ScalatraFilter with ResourceSupport with ServicesProvider {

  val log = LoggerFactory.getLogger(classOf[CurriculumFilter])

  var locale = Locale.FRANCE

  /**
   * test
   */
  get("/test") {
    log.debug("Test called!")
    <h1>Server up!
      {new Date().formatted("yyyy/MM/dd HH:mm:ss-SSS Z")}
    </h1>
  }

  /**
   *
   */
  get("/msg/list/*") {
    val what = params("splat")
    log.debug("Message list queried: {}, {}", params, what)
    val lowerBound = what.toLong
    val msgs = messageQueue.listMessages(lowerBound).sortWith(_.messageId < _.messageId).map(_.toJSON(locale))
    contentType = "text/json"
    msgs.mkString("[", ",", "]")
  }

  /**
   * Start a node
   */
  post("/cluster/start") {
    log.debug("Cluster node start: {}", params)
    val nodeName = params("start-node-name")
    val nodePort = params("start-node-port")
    val node = ClusterNode(nodeName, nodePort.toInt, Map[String, Any]())
    val task = taskService.spawn(clusterService.startNodeRunnable(node))
    messageQueue.publish(TaskMessage.taskScheduled(task, ClusterMessage.nodeStarting(node)))
    response.setStatus(200)
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
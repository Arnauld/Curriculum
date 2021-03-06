package curriculum.web

import org.scalatra._
import org.slf4j.LoggerFactory
import page.Layout
import curriculum.domain.CurriculumVitaeInstances
import curriculum.domain.web.CurriculumVitaePage
import curriculum.cluster.page.ClusterAdminPage
import java.net.HttpURLConnection
import java.util.{Locale, Date}
import curriculum.cluster.{ClusterMessage, ClusterNode}
import curriculum.message.MessageQueue
import curriculum.eav.Instance
import org.apache.commons.httpclient.HttpStatus
import curriculum.util.{ToJSON, SearchParameters, Bytes, Strings}
import curriculum.eav.service.{SearchBySimilitude, WeightedInstance, SearchMessage}
import javax.servlet.FilterConfig
import curriculum.eav.service.web.WebMessage
import xml.NodeSeq

class CurriculumFilter extends ScalatraFilter with ResourceSupport with ServicesProvider {

  val log = LoggerFactory.getLogger(classOf[CurriculumFilter])

  var locale = Locale.FRANCE

  override def init(filterConfig: FilterConfig) {
    super.init(filterConfig)
    log.info("Filter started!")
  }

  override def destroy() {
    log.info("Filter Destoyed!")
    clusterService.dispose()
    super.destroy()
  }

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
   * Query message
   */
  get("/msg/list/*") {
    try {
        val what = params("splat")
        log.debug("Message list queried: {}, {}", params, what)
        val lowerBound = what.toLong
        var msgs = MessageQueue.Local.listMessages(lowerBound)
        var retry = 1
        while(msgs.isEmpty && retry<3) {
          retry = retry+1
          Thread.sleep(500)
          msgs = MessageQueue.Local.listMessages(lowerBound, 1)
        }
        contentType = "text/json"
        ToJSON.toJSONString(msgs)
    } catch {
      case e: Throwable =>
        log.error("Oooops", e)
        throw e
    }
  }

  /**
   * our clustered search engine
   */
  get("/search") {
    try {
      log.debug("Search called")
      readInstanceFromParams match {
        case Some(inst) =>
          val keywords = params.get("keywords").getOrElse("").split(",")
          val asyncResult = searchService.search(SearchBySimilitude(inst, keywords, new SearchParameters {}))
          asyncResult.setCallback({ result =>
            log.debug("Result received! ({} instances)", result.size)
            // let's build clickable results
            publishSearchResult(result)
          })
          response.setStatus(HttpStatus.SC_ACCEPTED)
          ""
        case None =>
          MessageQueue.Local.publish(SearchMessage.noSimilarInstanceDefined())
          response.setStatus(HttpStatus.SC_PRECONDITION_FAILED)
          ""
      }
    } catch {
      case e: Throwable =>
        log.error("Oooops", e)
        throw e
    }
  }

  /**
   * Start a node
   */
  post("/cluster/start") {
    try{
        log.debug("Cluster node start: {}", params)
        val nodeName = params("start-node-name")
        val nodePort = params("start-node-port")
        val node = ClusterNode(nodeName, nodePort.toInt, Map[String, Any]())
        val task = taskService.spawn(
          clusterService.startNodeRunnable(node),
          ClusterMessage.nodeStarting(node).toLocaleAware)
        response.setStatus(HttpStatus.SC_ACCEPTED)
        """{ "task_id":""" + task.taskId + "}";
    } catch {
      case e: Throwable =>
        log.error("Oooops", e)
        throw e
    }
  }

  /**
   * list nodes
   */
  get("/cluster/list") {
    try{
        log.debug("List of nodes queried")
        val nodes = clusterService.listNodes
        MessageQueue.Local.publish(ClusterMessage.nodesRunningWeb(nodes))
        response.setStatus(HttpStatus.SC_ACCEPTED)
        ""
    } catch {
      case e: Throwable =>
        log.error("Oooops", e)
        throw e
    }
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
  get("/show/1") {
    showCV
  }

  get("/arnauld") {
    showCV
  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  private def showCV:NodeSeq = {
    val instance = instanceReader.loadInstance(CurriculumVitaeInstances.Arnauld)

    /*
     * generate page
     */
    val page = new CurriculumVitaePage(instance)
    val layout = new Layout {}
    layout.render(page.content)
  }

  private def publishSearchResult(results:List[WeightedInstance]) {
    MessageQueue.Local.publish(SearchMessage.searchFinished(results.length))
    results.foreach({inst =>
      val msg = WebMessage.weightedInstanceLink(inst, "/show/%d")
      MessageQueue.Local.publish(msg)
    })
  }

  private def readInstanceFromParams: Option[Instance] = {
    entityService().getEntity(params("entity").asInstanceOf[String]) match {
      case None =>
        None
      case Some(e) =>
        val inst = e.newInstance
        e.getAttributes.values.foreach({
          a =>
            params.get("instance." + a.attributeName) match {
              case None => // no value for attributes
              case Some(v) =>
                inst.setAttributeValue(a.attributeName, v)
            }
        })
        Some(inst)
    }
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
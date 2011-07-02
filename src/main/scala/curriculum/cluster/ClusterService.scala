package curriculum.cluster

import netty.ClusterNodeNetty
import org.slf4j.LoggerFactory
import curriculum.util.{ProgressMonitor, RunnableWithProgress}

trait ClusterService {
  private val log = LoggerFactory.getLogger(classOf[ClusterService])
  private var nodes = Map[ClusterNode, ClusterNodeNetty]()

  // Map is immutable, return safely the keys view
  def listNodes: Iterable[ClusterNode] = nodes.keys

  def startNodeRunnable(node: ClusterNode) = new RunnableWithProgress {
    def run(monitor: ProgressMonitor) {
      startNode(node, monitor)
    }
  }

  def startNode(node: ClusterNode) {
    startNode(node, new ProgressMonitor {})
  }

  private def startNode(node: ClusterNode, monitor: ProgressMonitor) {
    monitor.beginTask("node_start", 4)

    monitor.subTask("check_node_alread_exists")
    val n = synchronized {
      // check if node is not already there
      nodes.find(_._1 == node) match {
        case None =>
          val nettyServer = new ClusterNodeNetty(node)
          nodes += (node -> nettyServer)
          nettyServer
        case Some(alreadyDefined) =>
          alreadyDefined._2
      }
    }
    monitor.worked(1)

    monitor.subTask("node_start")
    try {
      // if node is already started this have no effect
      n.start(monitor.subMonitor)
    }
    catch {
      case e: ClusterNodeException =>
        // simply rethrow it
        throw e
      case e: Exception =>
        log.error("Failed to start node <{}> on port {}", node.name, node.port)
        import ClusterNodeException._
        throw new ClusterNodeException(node, NodeStartError)
    }
  }
}

object ClusterNodeException {

  sealed trait Code

  case object NodeStartError extends Code

}

class ClusterNodeException(val node: ClusterNode, val code: ClusterNodeException.Code, cause: Throwable) extends Exception(cause) {
  def this(node: ClusterNode, code: ClusterNodeException.Code) = this (node, code, null)

  import ClusterNodeException._

  override def getMessage = code match {
    case NodeStartError => "Failed to start node <%s> on port %d".format(node.name, node.port)
    case _ => "Unknown error type for node <%s> on port %d".format(node.name, node.port)
  }
}
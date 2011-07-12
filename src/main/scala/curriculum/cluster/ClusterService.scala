package curriculum.cluster

import netty.ClusterNodeNetty
import org.slf4j.LoggerFactory
import curriculum.util.{ProgressMonitor, RunnableWithProgress}
import curriculum.message.MessageQueue
import java.net.BindException
import java.util.concurrent.{ConcurrentHashMap, LinkedBlockingQueue}
import java.util.concurrent.atomic.AtomicLong

trait ClusterService {
  private val log = LoggerFactory.getLogger(classOf[ClusterService])
  private var nodes = Map[ClusterNode, ClusterNodeNetty]()
  private val jobsToDispatch = new LinkedBlockingQueue[ClusterJob]
  private val dispatchedJob = new ConcurrentHashMap[Long,ClusterJob]()
  private val dispatchIdGen = new AtomicLong()

  def dispose () {
    nodes.values.foreach(_.stop())
  }

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

    MessageQueue.Local.publish(ClusterMessage.nodeStarting(node))

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

    import ClusterNodeException._
    monitor.subTask("node_start")
    try {
      // if node is already started this have no effect
      n.start(monitor.subMonitor)
      onNodeStart()
    }
    catch {
      case e: ClusterNodeException =>
        log.error("Failed to start node <%s> on port %d".format(node.name, node.port), e)
        MessageQueue.Local.publish(ClusterMessage.nodeStartError(node))
        // simply rethrow it
        throw e
      case e: BindException =>
        log.error("Failed to start node <%s> on port %d".format(node.name, node.port), e)
        MessageQueue.Local.publish(ClusterMessage.nodeBindError(node))
        throw new ClusterNodeException(node, NodeStartError)
      case e: Exception =>
        log.error("Failed to start node <%s> on port %d".format(node.name, node.port), e)
        MessageQueue.Local.publish(ClusterMessage.nodeStartError(node))
        throw new ClusterNodeException(node, NodeStartError)
    }
  }

  def onNodeStart() {
    // TODO: move me in an actor based to have a single and async thread that dispatch
    dispatchJobs()
  }

  def dispatchJob[T](job:ClusterJob) {
    jobsToDispatch.add(job)

    // TODO: move me in an actor based to have a single and async thread that dispatch
    dispatchJobs()
  }

  val roundRobin = new AtomicLong()

  private[cluster] def dispatchJobs() {
    log.debug("Dispatching jobs ({} in queue)", jobsToDispatch.size())

    val availables = this.listNodes.toArray
    val nbAvailable = availables.length
    if(nbAvailable==0) {
      MessageQueue.Local.publish(ClusterMessage.noNodeRunning())
    }
    else {
      var job = jobsToDispatch.poll()
      while(job!=null) {
        val choosenNodeIndex = (roundRobin.incrementAndGet()%nbAvailable).toInt
        val choosenNode = availables(choosenNodeIndex)
        val jobId = dispatchIdGen.incrementAndGet()

        log.debug("Dispatching job {} to {}", jobId, choosenNode.name)

        // keep dispatched job, waiting the response
        dispatchedJob.put(jobId, job)
        Http.post(jobId, choosenNode, job)

        //
        job = jobsToDispatch.poll()
      }
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
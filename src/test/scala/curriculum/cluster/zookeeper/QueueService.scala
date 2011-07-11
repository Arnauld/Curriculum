package curriculum.cluster.zookeeper

import org.slf4j.LoggerFactory
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.KeeperException.NodeExistsException
import com.sun.xml.internal.ws.developer.MemberSubmissionAddressing.Validation
import org.apache.zookeeper.data.Stat

trait QueueService {
  self: ZookeeperComponent with ZookeeperSupport =>

  private val log = LoggerFactory.getLogger(classOf[QueueService])
  private val elementSubPath = "element-"
  private val queueRoot = "/queues"

  def pathForQueue(queueName:String) = queueRoot + "/" + queueName

  /**
   * Create a queue with name <code>queueName</code> if it does not already exists.
   */
  def createQueue(queueName: String) {
    val zk = zookeeperOrFail
    val path = pathForQueue(queueName)
    try {
      val stat = zk.exists(path, false)
      if (stat == null) {
        createAllIntermediaryMissingNodes(zk, path, persistentEmptyNodeCreator)
        log.info("Queue " + queueName + " created")
      }
      else {
        log.info("Queue <{}> already exists: creation skipped", queueName)
      }
    }
    catch {
      case e: NodeExistsException =>
        log.info("Queue <{}> already exists: creation skipped", queueName)
      case e: Exception =>
        log.error("Failed to create node <" + queueName + ">", e)
        // rethrow it: let's the caller handle it
        throw e
    }
  }

  /**
   *
   */
  def publish(queueName: String, data: Array[Byte]) {
    val zk = zookeeperOrFail
    val path = pathForQueue(queueName) + "/" + elementSubPath
    val created = zk.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL)
    log.info("Message <{}> published on queue <{}>", created, queueName)
  }

  private def extractSequence(childName: String) =
    childName.substring(elementSubPath.length()).toLong

  import scala.collection.JavaConversions._

  /**
   *
   */
  def consumeOne(queueName: String): Option[Array[Byte]] = {
    val root = pathForQueue(queueName)
    val zk = zookeeperOrFail
    val children = zk.getChildren(root, true);
    if (children.isEmpty) {
      None
    }
    else {
      // children are returned in lexical order and not in sequence order
      // thus one iterate over them to find the lowest sequence by extracting it
      // from their names
      val initial = extractSequence(children.get(0))
      val min = children.foldLeft(initial)({
        (prev, childName) =>
          val seq = extractSequence(childName)
          if (seq < prev)
            seq
          else
            prev
      })
      log.debug("Child with sequence <{}> selected, retrieving data", min)

      val path = root + "/" + elementSubPath + min
      val stat:Stat = null
      val data = zk.getData(path, false, stat)
      zk.delete(path, 0)

      Some(data)
    }
  }

}
package curriculum.cluster.zookeeper

import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.{CreateMode, ZooKeeper}
import org.apache.zookeeper.KeeperException.NodeExistsException
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

trait ZookeeperSupport {

  private val log = LoggerFactory.getLogger(classOf[ZookeeperSupport])

  type NodeCb = (ZooKeeper, String) => Any

  def persistentEmptyNodeCreator: NodeCb = (zk: ZooKeeper, nodePath: String) => {
    zk.create(nodePath, null /*data*/ , Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
  }

  def getChildren(zk: ZooKeeper, path: String): Seq[String] = {
    zk.getChildren(path, false)
  }

  def delete(zk: ZooKeeper, path: String) {
    zk.delete(path, -1)
  }

  def deleteRecursive(zk: ZooKeeper, path: String) {
    val children = getChildren(zk, path)
    for (node <- children) {
      deleteRecursive(zk, path + '/' + node)
    }
    delete(zk, path)
  }

  /**
   * mkdir -p
   */
  def createAllIntermediaryMissingNodes(zk: ZooKeeper, nodePath: String, creator: NodeCb) {
    val parts = (
      if (nodePath.charAt(0) == '/')
        nodePath.substring(1)
      else
        nodePath).split("/")
    
    log.debug("Path <{}> splitted into {}", nodePath, parts)
    parts.foldLeft("")({
      (base, fragment) =>
        val path = base + "/" + fragment
        createNodeIfMissing(zk, path, creator)
        path
    })
  }

  /**
   * Assumption is done that the parent exists
   */
  def createNodeIfMissing(zk: ZooKeeper, nodePath: String, creator: NodeCb) {
    try {
      val stat = zk.exists(nodePath, false)
      if (stat == null) {
        val created = creator(zk, nodePath)
        log.info("Node <{}> created at <{}>", nodePath, created)
      }
      else {
        log.info("Node <{}> already exists: creation skipped", nodePath)
      }
    }
    catch {
      case e: NodeExistsException =>
        log.info("Node <{}> already exists: creation skipped", nodePath)
      case e: Exception =>
        log.error("Failed to create node <" + nodePath + ">", e)
        // rethrow it: let's the caller handle it
        throw e
    }
  }
}
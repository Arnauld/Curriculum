package curriculum.cluster.zookeeper

import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.KeeperException.NodeExistsException
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import org.apache.zookeeper.data.Stat
import org.apache.zookeeper.{Watcher, CreateMode, ZooKeeper}

trait ZookeeperSupport {

  private val log = LoggerFactory.getLogger(classOf[ZookeeperSupport])

  type NodeCb = (ZooKeeper, String) => Any

  def persistentEmptyNodeCreator: NodeCb = (zk: ZooKeeper, nodePath: String) => {
    zk.create(nodePath, null /*data*/ , Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
  }

  def getChildren(zk: ZooKeeper, path: String): Seq[String] = {
    zk.getChildren(path, false)
  }

  def watchChildren(zk: ZooKeeper, path: String, watcher:Watcher) {
    val stat:Stat = null
    zk.getChildren(path, watcher, stat)
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
   * create all the necessary node to reach the nodePath
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
   * Create a node if it doesn't exist yet.
   * Assumption is done that the parent exists.
   * @see #createAllIntermediaryMissingNodes
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

  def getData(zk: ZooKeeper, nodePath: String): Array[Byte] = {
    val stat:Stat = null
    zk.getData(nodePath, false, stat)
  }

  def setData(zk: ZooKeeper, nodePath: String, data: Array[Byte]) {
    zk.setData(nodePath, data, -1)
  }
}
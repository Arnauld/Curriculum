package curriculum.cluster.zookeeper

import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.CreateMode
import org.slf4j.LoggerFactory
import org.apache.zookeeper.KeeperException.NodeExistsException

trait GroupService {
  self: Connection with ZookeeperSupport =>

  private val log = LoggerFactory.getLogger(classOf[GroupService])

  var formatGroupPath =
    (groupName:String) => "/" + groupName
  var formatMemberPath =
    (groupName:String, memberName:String) => formatGroupPath(groupName) + "/" + memberName

  /**
   * Create a group with name <code>queueName</code> if it does not already exists.
   */
  def createGroup(groupName: String) {
    val zk = zookeeperOrFail
    val path = formatGroupPath(groupName)
    try {

      val stat = zk.exists(path, false)
      if (stat == null) {
        val created = zk.create(path, null /*data*/ , Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
        log.info("Group " + groupName + " created on path <" + created + ">")
      }
      else {
        log.info("Group <{}> already exists: creation skipped", groupName)
      }
    }
    catch {
      case e: NodeExistsException =>
        log.info("Group <{}> already exists: creation skipped", groupName)
      case e: Exception =>
        log.error("Failed to create node <" + groupName + ">", e)
        // rethrow it: let's the caller handle it
        throw e
    }
  }

  def deleteGroup(groupName: String) {
    try {
      val zk = zookeeperOrFail
      val path = formatGroupPath(groupName)
      deleteRecursive(zk, path)
      log.info("Group <{}> deleted", groupName)
    }
    catch {
      case e: Exception =>
        log.error("Failed to delete group <" + groupName + ">", e)
        throw e
    }
  }

  def joinGroup(groupName: String, memberName: String) {
    try {
      val zk = zookeeperOrFail
      val path = formatMemberPath(groupName, memberName)
      val created = zk.create(path, null /*data*/ , Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
      log.info("<" + memberName + "> joined group <" + groupName + "> on path <" + created + ">")
    }
    catch {
      case e: Exception =>
        log.error("Failed add <" + memberName + "> to group <" + groupName + ">", e)
        throw e
    }
  }

  def leaveGroup(groupName: String, memberName: String) {
    try {
      val zk = zookeeperOrFail
      val path = formatMemberPath(groupName, memberName)
      delete(zk, path)
      log.info("<" + memberName + "> left group <" + groupName + ">")
    }
    catch {
      case e: Exception =>
        log.error("Failed to remove <" + memberName + "> from group <" + groupName + ">", e)
        throw e
    }
  }

  def listGroupMembers(groupName: String): Seq[String] = {
    try {
      val zk = zookeeperOrFail
      val path = formatGroupPath(groupName)
      getChildren(zk, path)
    }
    catch {
      case e: Exception =>
        log.error("Failed to retrieve members of group <" + groupName + ">", e)
        throw e
    }
  }

}
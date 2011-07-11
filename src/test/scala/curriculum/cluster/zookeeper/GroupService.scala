package curriculum.cluster.zookeeper

import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.CreateMode
import org.slf4j.LoggerFactory
import org.apache.zookeeper.KeeperException.NodeExistsException

trait GroupService {
  self:Connection =>

  private val log = LoggerFactory.getLogger(classOf[GroupService])

  /**
   * Create a group with name <code>queueName</code> if it does not already exists.
   */
  def createGroup(groupName:String) {
    val zk = zookeeperOrFail
    val path = "/" + groupName
    try {

      val stat = zk.exists(path, false)
      if(stat==null) {
        val created = zk.create(path, null/*data*/, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
        log.info("Group " + groupName + " created on path <" + created + ">")
      }
      else {
        log.info("Group <{}> already exists: creation skipped", groupName)
      }
    }
    catch {
      case e:NodeExistsException =>
        log.info("Group <{}> already exists: creation skipped", groupName)
      case e:Exception =>
        log.error("Failed to create node <" + groupName +">", e)
        // rethrow it: let's the caller handle it
        throw e
    }
  }

  def deleteGroup(groupName:String) {
    val zk = zookeeperOrFail
    val path = "/" + groupName
    zk.delete(path, -1)
    log.info("Group <{}> deleted", groupName)
  }

  def joinGroup(groupName:String, memberName:String) {
    val zk = zookeeperOrFail
    val path = "/" + groupName + "/" + memberName
    val created = zk.create(path, null/*data*/, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
    log.info("<"+memberName + "> joined group <" + groupName + "> on path <" + created + ">")
  }

  def leaveGroup(groupName:String, memberName:String) {
    val zk = zookeeperOrFail
    val path = "/" + groupName + "/" + memberName
    zk.delete(path, -1)
    log.info("<"+memberName + "> left group <" + groupName + ">")
  }

  def listGroupMembers(groupName:String):Array[String] = {
    val zk = zookeeperOrFail
    val path = "/" + groupName
    val members = zk.getChildren(path, false)
    members.toArray(new Array[String](members.size()))
  }

}
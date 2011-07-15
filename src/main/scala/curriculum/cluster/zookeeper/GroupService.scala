package curriculum.cluster.zookeeper

import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.CreateMode
import org.slf4j.LoggerFactory
import org.apache.zookeeper.KeeperException.NodeExistsException

trait Group {
  def service:GroupService
  def groupName:String
  def createGroup() {
    service.createGroup(groupName)
  }
  def deleteGroup() {
    service.deleteGroup(groupName)
  }
  def addMember(memberName:String) {
    service.joinGroup(groupName, memberName)
  }
  def removeMember(memberName:String) {
    service.leaveGroup(groupName, memberName)
  }
  def listMembers:Seq[String] = {
    service.listGroupMembers(groupName)
  }
}

trait GroupService {
  self: ZookeeperSupport =>

  private val log = LoggerFactory.getLogger(classOf[GroupService])

  var formatGroupPath =
    (groupName:String) => "/" + groupName
  var formatMemberPath =
    (groupName:String, memberName:String) => formatGroupPath(groupName) + "/" + memberName

  def groupHandle(_groupName:String) = new Group {
    def groupName = _groupName
    def service = GroupService.this
  }

  /**
   * Create a group with name <code>queueName</code> if it does not already exists.
   */
  def createGroup(groupName: String) {
    val path = formatGroupPath(groupName)
    try {
      if (exists(path)) {
        log.info("Group <{}> already exists: creation skipped", groupName)
      }
      else {
        val created = createPersistent(path)
        log.info("Group " + groupName + " created on path <" + created + ">")
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

  /**
   * Delete the group, all subgroups and all its members.
   */
  def deleteGroup(groupName: String) {
    try {
      val path = formatGroupPath(groupName)
      deleteRecursive(path)
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
      val path = formatMemberPath(groupName, memberName)
      val created = createEphemeral(path)
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
      val path = formatMemberPath(groupName, memberName)
      delete(path)
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
      val path = formatGroupPath(groupName)
      getChildren(path)
    }
    catch {
      case e: Exception =>
        log.error("Failed to retrieve members of group <" + groupName + ">", e)
        throw e
    }
  }

}
package curriculum.cluster.zookeeper

import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.KeeperException.NodeExistsException
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import org.apache.zookeeper.data.Stat
import org.apache.zookeeper.{KeeperException, Watcher, CreateMode}

trait ZookeeperSupport {
  self:ZookeeperComponent =>

  private val log = LoggerFactory.getLogger(classOf[ZookeeperSupport])

  def exists(path:String):Boolean = {
    zookeeperOrFail.exists(path, false) != null
  }

  def getChildren(path: String): Seq[String] = {
    zookeeperOrFail.getChildren(path, false)
  }

  def getData(nodePath: String): Array[Byte] = {
    val stat: Stat = null
    zookeeperOrFail.getData(nodePath, false, stat)
  }

  def setData(nodePath: String, data: Array[Byte]) {
    zookeeperOrFail.setData(nodePath, data, -1)
  }

  def watchChildren(path: String, watcher: Watcher) {
    val stat: Stat = null
    zookeeperOrFail.getChildren(path, watcher, stat)
  }

  def delete(path: String) {
    zookeeperOrFail.delete(path, -1)
  }

  def deleteRecursive(path: String) {
    val children = getChildren(path)
    for (node <- children) {
      deleteRecursive(path + '/' + node)
    }
    delete(path)
  }

  /*~~~~~~~~~~~~~~~~~~~~~~~~PERSISTENT~~~~~~~~~~~~~~~~~~~~~~~~*/

  def createPersistent(path: String):String =
    createPersistent(path, false)

  def createPersistent(path: String, createParents: Boolean):String =
    create(path, CreateMode.PERSISTENT, createParents)

  def createPersistentSequential(path: String):String =
    createPersistentSequential(path, false)

  def createPersistentSequential(path: String, createParents: Boolean):String =
    create(path, CreateMode.PERSISTENT_SEQUENTIAL, createParents)

  def createPersistent(path: String, data:Array[Byte]):String =
    createPersistent(path, data, false)

  def createPersistent(path: String, data:Array[Byte], createParents: Boolean):String =
    create(path, data, CreateMode.PERSISTENT, createParents)

  def createPersistentSequential(path: String, data:Array[Byte]):String =
    createPersistentSequential(path, data, false)

  def createPersistentSequential(path: String, data:Array[Byte], createParents: Boolean):String =
    create(path, data, CreateMode.PERSISTENT_SEQUENTIAL, createParents)


  /*~~~~~~~~~~~~~~~~~~~~~~~~EPHEMERAL~~~~~~~~~~~~~~~~~~~~~~~~*/

  def createEphemeral(path: String):String =
    createEphemeral(path, false)

  def createEphemeral(path: String, createParents: Boolean):String =
    create(path, CreateMode.EPHEMERAL, createParents)

  def createEphemeralSequential(path: String):String =
    createEphemeralSequential(path, false)

  def createEphemeralSequential(path: String, createParents: Boolean):String =
    create(path, CreateMode.EPHEMERAL_SEQUENTIAL, createParents)

  def createEphemeral(path: String, data:Array[Byte]):String =
    createEphemeral(path, data, false)

  def createEphemeral(path: String, data:Array[Byte], createParents: Boolean):String =
    create(path, data, CreateMode.EPHEMERAL, createParents)

  def createEphemeralSequential(path: String, data:Array[Byte]):String =
    createEphemeralSequential(path, data, false)

  def createEphemeralSequential(path: String, data:Array[Byte], createParents: Boolean):String =
    create(path, data, CreateMode.EPHEMERAL_SEQUENTIAL, createParents)

  /*~~~~~~~~~~~~~~~~~~~~~~~~SHAREd~~~~~~~~~~~~~~~~~~~~~~~~*/

  def create(path: String, createMode: CreateMode, createParents: Boolean):String =
    create(path, null/*data*/, createMode, createParents)

  def create(path: String, data:Array[Byte], createMode: CreateMode, createParents: Boolean):String =
    try {
      rawCreate(path, data, createMode)
    }
    catch {
      case e: KeeperException =>
        if (e.code()==KeeperException.Code.NONODE && createParents) {
          parentPathOf(path) match {
            case Some(p) =>
              // create parent, notes that it is recursive :)
              create(p, createMode, createParents)
              // retry
              rawCreate(path, data, createMode)
            case _ =>
              log.error("No parent path retrieved from {}", path)
              throw e
          }
        }
        else {
          log.error("Not in missing parent path case using {}", path)
          throw e
        }
      case otherKindOfException =>
        log.error("Cannot create node at {}", path)
        throw otherKindOfException
    }

  private[zookeeper] def rawCreate(path: String, data: Array[Byte], createMode: CreateMode):String =
    zookeeperOrFail.create(path, data, Ids.OPEN_ACL_UNSAFE, createMode)

  def parentPathOf(fullpath: String):Option[String] = fullpath.lastIndexOf('/') match {
    case x if (x == 0) => None //root case
    case x if (x <= 0) => throw new IllegalArgumentException("Invalid path, does not at least start with '/'")
    case x => Some(fullpath.substring(0, x))
  }


  /**
   * create all the necessary node to reach the nodePath.
   * Since ephemeral nodes cannot contains sub-nodes, all nodes created through
   * this method are PERSISTENT.
   */
  def ensurePersistentPathExists(nodePath: String) {
    val parts = (
      if (nodePath.charAt(0) == '/')
        nodePath.substring(1)
      else
        nodePath).split("/")

    log.debug("Path <{}> splitted into {}", nodePath, parts)
    parts.foldLeft("")({
      (base, fragment) =>
        val path = base + "/" + fragment
        createPersistentIfMissing(path)
        path
    })
  }

  /**
   * Create a node if it doesn't exist yet.
   * Assumption is done that the parent exists.
   * @see #ensurePersistentPathExists
   */
  def createPersistentIfMissing(nodePath: String) {
    try {
      if (exists(nodePath)) {
        log.info("Node <{}> already exists: creation skipped", nodePath)
      }
      else{
        val created = createPersistent(nodePath)
        log.info("Node <{}> created at <{}>", nodePath, created)
      }
    }
    catch {
      case e: NodeExistsException =>
        // created in the meanwhile
        log.info("Node <{}> already exists: creation skipped", nodePath)
      case e: Exception =>
        log.error("Failed to create node <" + nodePath + ">", e)
        // rethrow it: let's the caller handle it
        throw e
    }
  }

}
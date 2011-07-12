package curriculum.cluster.zookeeper

import java.util.concurrent.CountDownLatch
import org.apache.zookeeper.Watcher.Event.KeeperState
import org.slf4j.LoggerFactory
import curriculum.util.Disposable
import org.apache.zookeeper.{ZooKeeper, WatchedEvent, Watcher}

/**
 * Base class to help connecting with zookeeper
 */
class ZookeeperAccessException(message:String) extends Exception(message)


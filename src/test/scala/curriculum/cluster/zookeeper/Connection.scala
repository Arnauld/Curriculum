package curriculum.cluster.zookeeper

import java.util.concurrent.CountDownLatch
import org.apache.zookeeper.Watcher.Event.KeeperState
import org.slf4j.LoggerFactory
import curriculum.util.Disposable
import org.apache.zookeeper.{ZooKeeper, WatchedEvent, Watcher}

/**
 * Base class to help connecting with zookeeper
 */
class Connection extends ZookeeperComponent with Disposable {

  private val log = LoggerFactory.getLogger(classOf[Connection])

  var sessionTimeout = 5000

  protected var zk:Option[ZooKeeper] = None

  def zookeeper = zk

  /**
   * Connect to <code>hosts</code>.
   *
   * This method blocks until the connection is established
   */
  def connect(connectString: String) {
    val connectedSignal = new CountDownLatch(1)
    val zookeeper = new ZooKeeper(connectString, sessionTimeout, new Watcher {
      def process(event: WatchedEvent) {
        log.info("Event received: " + event)
        if(event.getState == KeeperState.SyncConnected) {
          connectedSignal.countDown()
        }
      }
    })
    connectedSignal.await()
    zk = Some(zookeeper)
  }

  def dispose() {
    zk.foreach(_.close())
  }

}


package curriculum.cluster.zookeeper

import org.slf4j.LoggerFactory
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.KeeperException.NodeExistsException
import java.util.concurrent.locks.ReentrantLock
import org.apache.zookeeper.{WatchedEvent, Watcher, CreateMode}
import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.concurrent.atomic.{AtomicLong, AtomicBoolean}
import scala.collection.JavaConversions._


trait QueueListener {
  def onEvent(data: Array[Byte])
}








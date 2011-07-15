package curriculum.cluster.zookeeper

import org.slf4j.LoggerFactory
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.KeeperException.NodeExistsException
import java.util.concurrent.locks.ReentrantLock
import org.apache.zookeeper.{WatchedEvent, Watcher, CreateMode}
import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.concurrent.atomic.{AtomicLong, AtomicBoolean}
import scala.collection.JavaConversions._


trait QueueService {
  self: ZookeeperSupport =>

  private val log = LoggerFactory.getLogger(classOf[QueueService])
  private val elementSubPath = "element-"
  private val queueRoot = "/queues"

  private var queues: Map[String, Queue] = Map.empty

  def pathForQueue(queueName: String) = queueRoot + "/" + queueName

  def pathForEntry(queueName: String, sequence: String) = pathForQueue(queueName) + "/" + elementSubPath + sequence

  /**
   * Create a queue with name <code>queueName</code> if it does not already exists.
   */
  def createQueue(queueName: String) {
    createQueueOnZookeeper(queueName)
  }

  private[zookeeper] def createQueueOnZookeeper(queueName: String) {
    val path = pathForQueue(queueName)
    try {
      if (exists(path)) {
        log.info("Queue <{}> already exists: creation skipped", queueName)
      }
      else {
        ensurePersistentPathExists(path)
        log.info("Queue " + queueName + " created")
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
    val path = pathForQueue(queueName) + "/" + elementSubPath
    val created = createPersistentSequential(path, data, true)
    log.info("Message <{}> published on queue <{}>", created, queueName)
  }

  private def extractSequence(childName: String) = {
    val seq = childName.substring(elementSubPath.length())
    (seq, seq.toLong)
  }

  /**
   *
   */
  def hasEvents(queueName: String): Boolean = {
    val root = pathForQueue(queueName)
    val children = getChildren(root);
    !children.isEmpty
  }

  /**
   *
   */
  def consumeOne(queueName: String): Option[Array[Byte]] = {
    val root = pathForQueue(queueName)
    val children = getChildren(root);
    if (children.isEmpty) {
      None
    }
    else {
      // The list of children returned is not sorted and no guarantee is provided
      // as to its natural or lexical order.
      // Thus one iterate over them to find the lowest sequence by extracting it
      // from their names
      val initial = extractSequence(children.get(0))
      val min = children.foldLeft(initial)({
        (prev, childName) =>
          val seq = extractSequence(childName)
          if (seq._2 < prev._2)
            seq
          else
            prev
      })
      log.debug("Child with sequence <{}> selected, retrieving data", min._1)

      val path = pathForEntry(queueName, min._1)
      val data = getData(path)
      delete(path)

      Some(data)
    }
  }

  /**
   * Get a queue. If the queue does not exists yet it is created.
   */
  def getQueue(queueName: String): Queue = synchronized {
    queues.get(queueName).getOrElse({
      log.debug("Queue "+queueName+" not registered yet", new Exception("current stack"))
      val q = new Queue(queueName, this, this)
      queues += (queueName -> q)

      log.debug("Queue registry containing {}? {}", queueName, queues.contains(queueName))
      // create the corresponding node
      createQueueOnZookeeper(queueName)
      q.start()
      q
    })
  }
}

object Queue {
  private val log = LoggerFactory.getLogger(classOf[Queue])

  val queueThreadGroup = new ThreadGroup("QueueThreads") {
    override def uncaughtException(t: Thread, e: Throwable) {
      log.error("Error on queue <" + t.getName +">", e)
    }
  }
  val seqGen = new AtomicLong()
}

class Queue(val queueName: String,
            val queueService:QueueService,
            private val zk:ZookeeperSupport) {

  private val qlog = LoggerFactory.getLogger(classOf[Queue])

  import curriculum.util.LockSupport._

  var listeners: List[QueueListener] = Nil
  val running = new AtomicBoolean
  var waitTimeoutMillis = 100

  //
  implicit val lock = new ReentrantLock()
  val noListener = lock.newCondition()

  def addListener(listener: QueueListener) {
    qlog.debug("Registering listener on queue {}", queueName)
    withinLock({
      listeners = listener :: listeners
      noListener.signal()
    })
    qlog.debug("QueueListener registered on queue {}", queueName)
  }

  def removeListener(listener: QueueListener) {
    withinLock({
      listeners = listeners.filter(_ != listener)
      noListener.signal()
    })
  }

  def hasEvents: Boolean = queueService.hasEvents(queueName)

  def publish(payload: Array[Byte]) {
    queueService.publish(queueName, payload)
  }

  def consumeOne(): Option[Array[Byte]] = {
    queueService.consumeOne(queueName)
  }

  private var threadOpt:Option[Thread] = None

  private[zookeeper] def start() {
    running.get() match {
      case false =>
        val thread = new Thread(Queue.queueThreadGroup, new Runnable {
          def run() {
            qlog.info("Starting queue {} ({})", queueName, Thread.currentThread())
            try {
              loop()
            }
            finally {
              qlog.info("Queue {} stopped! ({})", queueName, Thread.currentThread())
            }
          }
        }, queueName + "-" + Queue.seqGen.incrementAndGet())
        threadOpt = Some(thread)
        thread.start()
        qlog.debug("Queue {} thread started: {}", queueName, thread)
      case true =>
        qlog.debug("Queue {} already running", queueName)
    }
  }

  private[zookeeper] def stop() {
    qlog.info("Stoping queue {}", queueName)
    running.set(false)
    withinLock({
      // wake up every body
      noListener.signal()
    });

    threadOpt.foreach({ t =>
      qlog.debug("Waiting for {} to stop", t)
      t.join()
    })
  }

  private def loop() {
    running.set(true)

    var currentLatch: Option[CountDownLatch] = None
    while (running.get) {
      var lstnrs: List[QueueListener] = Nil

      withinLock({
        if (listeners.isEmpty) {
          qlog.debug("Waiting for listener registration on queue {}", queueName)
          noListener.await(1, TimeUnit.SECONDS) // prevent at least a for ever wait
        }
        lstnrs = listeners
      })

      if (lstnrs.isEmpty) {
        qlog.debug("No listeners registered on queue {}", queueName)
      }
      else {
        qlog.debug("At least one listener registered on queue {}, checking for event", queueName)

        // one has listeners, now try to retrieve an event...
        consumeOne() match {
          case None =>
            // no event: wait until a new event is added to the queue or wait timeout
            qlog.debug("No event yet in queue {}, wait for new event to dispatch", queueName)

            val latch = currentLatch match {
              // prevent multiple latch, if one is already defined, just reuse it as barrier
              case Some(cdl) =>
                cdl
              case None =>
                val cdl = new CountDownLatch(1)
                zk.watchChildren(queueService.pathForQueue(queueName), new Watcher {
                  def process(event: WatchedEvent) {
                    cdl.countDown()
                  }
                })
                currentLatch = Some(cdl)
                cdl
            }
            // wait until timeout is exceeded or a watch event has been triggered
            qlog.debug("Waiting for event publishing on queue {}", queueName)
            latch.await(waitTimeoutMillis, TimeUnit.MILLISECONDS)
          case Some(e) =>
            qlog.debug("Notifying #{} listener(s) for event on queue {}", lstnrs.size, queueName)
            // finally: listeners AND event!
            lstnrs.foreach({
              _.onEvent(e)
            })
        }
      }
    }
  }
}


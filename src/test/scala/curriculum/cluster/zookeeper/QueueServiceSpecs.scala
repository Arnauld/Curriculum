package curriculum.cluster.zookeeper

import org.specs.Specification
import org.slf4j.LoggerFactory
import java.util.concurrent.CyclicBarrier

class QueueServiceSpecs extends Specification with ZookeeperSpecsSupport {
  val log = LoggerFactory.getLogger(classOf[ZookeeperSpecs])
  var connectionOpt: Option[Connection] = None
  var queueOpt: Option[Queue] = None

  val queueName = "Magohamoth"
  val data = List("Lanfeust", "Hebus", "Cixi", "C'ian", "Nicolede")
  var listener: Collector = null

  override def clientPort = 2182

  def publishData(queue: Queue) {
    data.foreach({
      e =>
        queue.publish(e.getBytes("utf-8"))
    })
  }

  "QueueService" should {
    doBefore({
      resetZookeeperData()
      startZookeeperServer()
      listener = new Collector
    })
    doAfter({
      queueOpt.foreach(_.stop())
      connectionOpt.foreach(_.dispose())
      stopZookeeperServer()
    })

    "work in simple case: listener registered after events" in {
      val service = new Connection with QueueService with ZookeeperSupport
      connectionOpt = Some(service)
      service.connect("localhost:" + clientPort)

      val queue = service.getQueue(queueName)
      queueOpt = Some(queue)
      publishData(queue)

      queue.register(listener)

      while (queue.hasEvents) {
        Thread.sleep(50)
      }
      listener.events must containAll(data)
    }

    "work in simple case: listener registered before events" in {
      val service = new Connection with QueueService with ZookeeperSupport
      connectionOpt = Some(service)
      service.connect("localhost:" + clientPort)

      val queue = service.getQueue(queueName)
      queueOpt = Some(queue)
      val listener = new Collector
      queue.register(listener)

      publishData(queue)

      while (queue.hasEvents) {
        Thread.sleep(50)
      }
      listener.events must containAll(data)
    }

    "work in complex case: simulate the same queue used through multiple connection" in {
      val nbParticipants = 1
      val ccases = new ConcurrentCases(nbParticipants)

      0.to(nbParticipants).foreach({
        i =>
          new Thread(new Runnable {
            def run() {
              ccases.execute(i)
            }
          }).start
      })
      ccases.awaitTermination()
    }
  }

  class ConcurrentCases(val nbParticipants: Int = 5) {
    val beginBarrier = new CyclicBarrier(nbParticipants + 1)
    val endBarrier = new CyclicBarrier(nbParticipants + 1)

    def awaitTermination () {
      // unleash everybody
      beginBarrier.await()

      // wait for every body to finish
      endBarrier.await()
    }

    def execute(caseOrdinal: Int) {
      val service = new Connection with QueueService with ZookeeperSupport
      service.connect("localhost:" + clientPort)
      val queue = service.getQueue(queueName)
      val listener = new Collector
      try {
        beginBarrier.await()
        caseOrdinal.%(2) match {
          case 0 =>
            1.to(200).foreach({i => publishData(queue) })
          case 1 =>
            queue.register(listener)
        }
        endBarrier.await()
      }
      finally {
        queue.stop()
        service.dispose()
      }
    }
  }

  class Collector extends Listener {
    var events: List[String] = Nil

    def onEvent(data: Array[Byte]) {
      synchronized({
        events = new String(data) :: events
      })
    }
  }

}

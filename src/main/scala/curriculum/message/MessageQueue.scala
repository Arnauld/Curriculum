package curriculum.message

import java.util.concurrent.{TimeUnit, Executors}
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.{AtomicLong, AtomicInteger}

object MessageQueue {
  var Local = new BasicMessageQueue("")
  def Remote = Local
}

trait MessageQueue {
  def queueName:String
  def listMessages(lowerId: Long, limit:Int = 10):List[Message]
  def publish(msg: Message)
}

class BasicMessageQueue(val queueName:String) extends MessageQueue {

  private val log = LoggerFactory.getLogger(classOf[MessageQueue])

  private val victor = Executors.newScheduledThreadPool(1)

  private var messages: List[Message] = Nil

  private val messageCount = new AtomicInteger()

  val idGen = new AtomicLong()

  def listMessages(lowerId: Long, limit:Int = 10):List[Message] = {
    val selected = messages.filter(_.messageId > lowerId).reverse
    val limited =
      if (selected.size > limit)
        selected.slice(0, limit)
      else
        selected
    log.debug("Querying message with Id greater than {}, found #{} (limit: {}, total: {})",
      Array[Any](lowerId, limited.size, limit, messageCount.get).map(_.asInstanceOf[AnyRef]))
    limited
  }

  def publish(msg: Message) {
    synchronized {
      msg.messageId = idGen.incrementAndGet()
      messages = msg :: messages

      // size computing can be inefficient
      if(log.isDebugEnabled) {
        log.debug("Publishing message {}: {}", msg.messageId, msg)
        log.debug("Queue has #{} messages: {}", messages.size, messages.map(_.messageId).mkString("[", ",", "]"))
      }
      messageCount.incrementAndGet()
    }
  }

  def start() {
    victor.scheduleAtFixedRate(new Runnable {
      def run() {
        markAndSweep()
      }
    }, 1L, 1L, TimeUnit.MINUTES)
  }

  private[message] def markAndSweep() {
    log.debug("Cleaning queue #{} messages", messageCount.get)

    // keep message for 5mins, and let's mark the older ones
    val threshold = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)

    //
    messages.foreach({
      m =>
        if (m.createdAt < threshold)
          m.marked = true
    })

    // now the sync' part: sweep
    synchronized {
      val (marked, notMarked) = messages.partition(_.marked)
      messages = notMarked
      messageCount.addAndGet(-marked.size)
    }
    log.debug("Queue cleaned, remaining #{} messages", messageCount.get)
  }

}
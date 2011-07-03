package curriculum.util

import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{TimeUnit, Executors}
import org.slf4j.LoggerFactory

object Message {

  abstract class Type(val code: String)

  object Type {

    case object Info extends Type("type-info")

    case object Warn extends Type("type-warning")

    case object Error extends Type("type-error")

    case object Success extends Type("type-sucess")

  }

  def info(messageCode: Message.Code, args: Any*) = new Message(Type.Info, messageCode, args: _*)

  def warn(messageCode: Message.Code, args: Any*) = new Message(Type.Warn, messageCode, args: _*)

  def error(messageCode: Message.Code, args: Any*) = new Message(Type.Error, messageCode, args: _*)

  def success(messageCode: Message.Code, args: Any*) = new Message(Type.Success, messageCode, args: _*)

  //
  def code(content: String) = Code(content)

  trait Code {
    def format(locale: Locale, args: Any*): String = rawMessage(locale).format(args: _*)

    def rawMessage(locale: Locale): String
  }

  object Code {
    def apply(content: String): Code = new Code {
      def rawMessage(locale: Locale) = content
    }

    def apply(content: Map[Locale, String], fallback: String): Code = new Code {
      def rawMessage(locale: Locale) = content.get(locale).getOrElse(fallback)
    }
  }

  val idGen = new AtomicLong()
}


case class Message(messageType: Message.Type, messageCode: Message.Code, args: Any*) extends LocaleAware {

  var marked = false

  val messageId = Message.idGen.incrementAndGet()

  val createdAt = System.currentTimeMillis()

  def adaptTo(locale: Locale) = {
    val adapted = args.map({
      a => a match {
        case l: LocaleAware => l.adaptTo(locale)
        case _ => a
      }
    })
    messageCode.format(locale, adapted: _*)
  }

  // ok... not the cleanest way to do so, but at least simple enough to continue for now...
  def toJSON(locale: Locale) =
    "{ \"id\":\"" + messageId + "\", \"type\":\"" + messageType.code + "\", \"message\":\"" + adaptTo(locale) + "\" }"
}

object MessageQueue {
  var Local = new MessageQueue {}
}

trait MessageQueue {

  private val log = LoggerFactory.getLogger(classOf[MessageQueue])

  private val victor = Executors.newScheduledThreadPool(1)

  private var messages: List[Message] = Nil

  def listMessages(lastMsg: Long, limit:Int = 10) = {
    log.debug("Querying message with Id greater than {}", lastMsg)
    val selected = messages.filter(_.messageId > lastMsg)
    val limited =
      if (selected.size > limit)
        selected.slice(0, limit)
      else
        selected
    log.debug("Querying message with Id greater than {}, found #{}", lastMsg, limited.size)
    limited
  }

  def publish(msg: Message) {
    log.debug("Publishing message {}: {}", msg.messageId, msg)
    synchronized {
      messages = msg :: messages
    }
  }

  def start() {
    victor.scheduleAtFixedRate(new Runnable {
      def run() {
        markAndSweep()
      }
    }, 1L, 1L, TimeUnit.MINUTES)
  }

  private def markAndSweep() {
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
      messages = messages.filterNot(_.marked)
    }
  }

}
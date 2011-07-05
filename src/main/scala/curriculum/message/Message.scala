package curriculum.message

import java.util.Locale
import java.io.Writer
import curriculum.util.{ToJSON, LocaleAware}
import org.codehaus.jackson.{JsonGenerator, JsonFactory}
import curriculum.util.ToJSON._

object Message {

  abstract class Type(val code: String)

  object Type {

    case object Info extends Type("type-info")

    case object Warn extends Type("type-warning")

    case object Error extends Type("type-error")

    case object Success extends Type("type-success")

    case object Unknown extends Type("type-unknown")

  }

  def info(messageCode: Message.Code, args: Any*) = new Message(Type.Info, messageCode, args: _*)

  def warn(messageCode: Message.Code, args: Any*) = new Message(Type.Warn, messageCode, args: _*)

  def error(messageCode: Message.Code, args: Any*) = new Message(Type.Error, messageCode, args: _*)

  def success(messageCode: Message.Code, args: Any*) = new Message(Type.Success, messageCode, args: _*)

  //
  def code(content: String) = Code(content)

  trait Code {
    def format(locale: Locale, args: Any*): String = rawMessage(locale).format(args: _*)

    def rawMessage(locale: Locale = Locale.getDefault): String
  }

  object Code {
    def apply(content: String): Code = new Code {
      def rawMessage(locale: Locale) = content

      override def toString = content
    }

    def apply(content: Map[Locale, String], fallback: String): Code = new Code {
      def rawMessage(locale: Locale) = content.get(locale).getOrElse(fallback)

      override def toString = fallback
    }
  }

  def localeAware(messageCode: Message.Code, args: Any*) = new LocaleAware {
    def adaptTo(locale: Locale) = messageCode.format(locale, args: _*)
  }
}

case class Message(messageType: Message.Type, messageCode: Message.Code, args: Any*) extends LocaleAware with ToJSON {

  var parameters = Map[String, Any]()

  def toLocaleAware = Message.localeAware(messageCode, args: _*)

  private[message] var marked = false

  private[message] var messageId = -1L

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

  def toJSONString(locale: Locale): String = toJSONString(Map[Any, Any](classOf[Locale] -> locale))

  def writeJSONContent(g: JsonGenerator, ctx: Map[Any, Any]) {
    val locale = ctx.get(classOf[Locale]).getOrElse(Locale.US).asInstanceOf[Locale]
    g.writeNumberField("id", messageId)
    g.writeStringField("type", messageType.code)
    g.writeStringField("message", adaptTo(locale))
    writeField("parameters", parameters)(g)
  }
}

package curriculum.util

import java.util.Locale

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

}


case class Message(messageType: Message.Type, messageCode: Message.Code, args: Any*) extends LocaleAware {

  def adaptTo(locale: Locale) = {
    val adapted = args.map({ a => a match {
      case l:LocaleAware => l.adaptTo(locale)
      case _ => a
    }})
    messageCode.format(locale, adapted: _*)
  }

  // ok... not the cleanest way to do so, but at least simple enough to continue for now...
  def toJSON(locale: Locale) =
    "{ \"type\":\"" + messageType.code + "\", \"message\":\"" + adaptTo(locale) + "\" }"
}
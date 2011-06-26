package curriculum.util

import java.util.Locale

object Locales {
  val LocaleR1 = "([^_]+)".r
  val LocaleR2 = "([^_]+)_([^_]+)".r
  val LocaleR3 = "([^_]+)_([^_]+)_([^_]+)".r


  def toLocale(value: String): Locale = value match {
    case LocaleR1(l) => new Locale(l)
    case LocaleR2(l, c) => new Locale(l, c)
    case LocaleR3(l, c, v) => new Locale(l, c, v)
    case _ => Locale.getDefault
  }
}
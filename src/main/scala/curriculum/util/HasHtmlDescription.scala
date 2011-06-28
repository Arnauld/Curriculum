package curriculum.util

import java.util.Locale

trait HasHtmlDescription {
  def defaultDescription: String

  private var descriptions = Map[Locale, String]()

  def hasDescription(locale: Locale) = descriptions.contains(locale)

  def getDescription(locale: Locale) = descriptions.get(locale) match {
    case Some(desc) => desc
    case _ => defaultDescription
  }

  def setDescription(locale: Locale, value:String) {
    descriptions += (locale -> value)
  }

  def getDescriptions = descriptions
}
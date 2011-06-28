package curriculum.util

import java.util.Locale
import xml.NodeSeq

trait HasHtmlDescription {
  def defaultHtmlDescription: NodeSeq

  private var descriptions = Map[Locale, NodeSeq]()

  def hasHtmlDescription(locale: Locale) = descriptions.contains(locale)

  def getHtmlDescription(locale: Locale) = descriptions.get(locale) match {
    case Some(desc) => desc
    case _ => defaultHtmlDescription
  }

  def setHtmlDescription(locale: Locale, value:NodeSeq) {
    descriptions += (locale -> value)
  }

  def getHtmlDescriptions = descriptions
}
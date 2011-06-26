package curriculum.util

import java.util.Locale

trait HasLabel {
  def defaultLabel: String

  private var labels = Map[Locale, String]()

  def hasLabel(locale: Locale) = labels.contains(locale)

  def getLabel(locale: Locale) = labels.get(locale) match {
    case Some(label) => label
    case _ => defaultLabel
  }

  def setLabel(locale: Locale, value:String) {
    labels += (locale -> value)
  }

  def getLabels = labels
}
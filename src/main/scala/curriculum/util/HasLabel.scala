package curriculum.util

import java.util.Locale

trait HasLabel {
  def defaultLabel: String

  val labels = scala.collection.mutable.Map[Locale, String]()

  def hasLabel(locale: Locale) = labels.contains(locale)

  def getLabel(locale: Locale) = labels.get(locale) match {
    case Some(label) => label
    case _ => defaultLabel
  }
}
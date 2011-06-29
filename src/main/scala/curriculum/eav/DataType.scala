package curriculum.eav

import org.joda.time.{Months, LocalDate}
import xml.NodeSeq

case class Ratio(numerator: Int, denominator: Int = 100) {
  def toPercent: Int = (numerator * 100) / denominator
}

case class LocalDateRange(min: Option[LocalDate], max: Option[LocalDate]) {
  def duration: (Int, Int) =
    if (min.isDefined && max.isDefined) {
      val months = Months.monthsBetween(min.get, max.get).getMonths
      val years = months / 12
      (months % 12, years)
    }
    else
      (0, 0)
}

case class InstanceRef(instanceId: Long)

sealed abstract class DataType(valueType: Class[_])

case object TextType extends DataType(classOf[String])

case object HtmlType extends DataType(classOf[NodeSeq])

case object LinkType extends DataType(classOf[String])

case object RatioType extends DataType(classOf[Ratio]) {
  val RatioRegex = """(\-?\d+)/(\d+)""".r
  val NumRegex = """(\-?\d+)""".r

  def parse(value: String): Option[Ratio] = value.trim() match {
    case RatioRegex(numerator, denominator) => Some(Ratio(numerator.toInt, denominator.toInt))
    case NumRegex(numerator) => Some(Ratio(numerator.toInt, 1))
    case _ => None
  }

}

case object LocalDateType extends DataType(classOf[LocalDate]) {
  val DateRegex = """([\d]{4})/([\d]{2})/([\d]{2})""".r

  def parse(value: String): Option[LocalDate] = value.trim() match {
    case DateRegex(year, month, day) => Some(new LocalDate(year.toInt, month.toInt, day.toInt))
    case _ => None
  }

}

case object LocalDateRangeType extends DataType(classOf[LocalDateRange]) {
  def parse(value: String): Option[LocalDateRange] = {
    val parts = value.split('-')
    val dates = parts.map(LocalDateType.parse(_))
    // assume if there is one part, it is the min
    dates.size match {
      case 0 => Some(LocalDateRange(None, None))
      case 1 => Some(LocalDateRange(dates(0), None))
      case _ => Some(LocalDateRange(dates(0), dates(1)))
    }
  }
}

case class EntityType(entityName: String) extends DataType(classOf[InstanceRef])

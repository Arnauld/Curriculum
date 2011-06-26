package curriculum.eav

import org.joda.time.{Months, LocalDate}
import xml.NodeSeq

case class Ratio(numerator: Int, denominator: Int = 100)
case class LocalDateRange(min: LocalDate, max: LocalDate) {
  def duration: (Int, Int) = {
    val months = Months.monthsBetween(min, max).getMonths
    val years = months / 12
    (months % 12, years)
  }
}
case class InstanceRef(instanceId: Long)

sealed abstract class DataType(valueType:Class[_])
case object TextType extends DataType(classOf[String])
case object HtmlType extends DataType(classOf[NodeSeq])
case object LinkType extends DataType(classOf[String])
case object RatioType extends DataType(classOf[Ratio])
case object LocalDateType extends DataType(classOf[LocalDate])
case object LocalDateRangeType extends DataType(classOf[LocalDateRange])
case class EntityType(entityName:String) extends DataType(classOf[InstanceRef])

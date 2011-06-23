package curriculum.eav

import org.joda.time.{Months, Interval, Duration, LocalDate}

sealed trait Value
object Value {
  case class Ratio(numerator:Int, denominator:Int = 100) extends Value
  case class Text(content:String) extends Value
  case class DayMonthYear(localeDate:LocalDate) extends Value
  case class DayMonthYearRange(min:LocalDate, max:LocalDate) extends Value {
    def duration:(Int,Int) = {
      val months = Months.monthsBetween(min, max).getMonths
      val years = months/12
      (months%12, years)
    }
  }
  case class InstanceRef(instanceId:Long) extends Value
}

object ValueImplicits {
  implicit def intsToRatio(t:(Int, Int)):Value.Ratio = Value.Ratio(t._1, t._2)
  implicit def stringToText(value:String):Value.Text = Value.Text(value)
  implicit def intsToDMY(t:(Int, Int, Int)):Value.DayMonthYear =
    Value.DayMonthYear(new LocalDate(t._1, t._2, t._3))
}

sealed trait DataType
object DataType {
  case object Ratio extends DataType
  case object Text extends DataType
  case object Date extends DataType
  case object DateRange extends DataType
  case class Choice(values:Value*) extends DataType
  case class EntityType(entityName:String) extends DataType
}
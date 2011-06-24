package curriculum.eav

import org.joda.time.{Months, LocalDate}
import org.slf4j.LoggerFactory

private[eav] object ValueClasses {
  // define dedicated variables for class in order to use them in pattern matching
  // see http://www.scala-lang.org/node/516
  val V_NumClass = classOf[Value.Num]
  val V_RatioClass = classOf[Value.Num]
  val V_TextClass = classOf[Value.Num]
  val V_DayMonthYearClass = classOf[Value.DayMonthYear]
  val V_DayMonthYearRangeClass = classOf[Value.DayMonthYearRange]
  val V_ChoiceClass = classOf[Value.Choice]
  val V_InstanceRefClass = classOf[Value.InstanceRef]
}

sealed trait Value
object Value {

  case class Num(value: Int) extends Value
  case class Ratio(numerator: Int, denominator: Int = 100) extends Value
  case class Text(content: String) extends Value
  case class DayMonthYear(localeDate: LocalDate) extends Value
  case class DayMonthYearRange(min: LocalDate, max: LocalDate) extends Value {
    def duration: (Int, Int) = {
      val months = Months.monthsBetween(min, max).getMonths
      val years = months / 12
      (months % 12, years)
    }
  }
  case class Choice(values: List[Value]) extends Value
  case class InstanceRef(instanceId: Long) extends Value
}

object ValueImplicits {
  implicit def intToNum(value: Int): Value.Num = Value.Num(value)
  implicit def intsToRatio(t: (Int, Int)): Value.Ratio = Value.Ratio(t._1, t._2)
  implicit def stringToText(value: String): Value.Text = Value.Text(value)
  implicit def intsToDMY(t: (Int, Int, Int)): Value.DayMonthYear =
    Value.DayMonthYear(new LocalDate(t._1, t._2, t._3))
}

sealed trait DataType
object DataType {
  case object Num extends DataType
  case object Ratio extends DataType
  case object Text extends DataType
  case object DayMonthYear extends DataType
  case object DayMonthYearRange extends DataType
  case class Choice(values: List[Value]) extends DataType
  case class EntityType(entityName: String) extends DataType
}


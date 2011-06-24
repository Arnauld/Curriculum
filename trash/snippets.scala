import org.joda.time.LocalDate





sealed abstract class DataType(val valueType: Class[_ <: Value]) {
  type converter = PartialFunction[(Value, Class[_ <: Value]), Option[Value]]
  val sanityAndIdentityConverter: converter = {
    case (value, _) if (value.getClass != valueType) =>
      log.warn("Incompatible input type got: <{}> expected <{}>", value.getClass, valueType)
      None
    case (value, `valueType`) => Some(value)
  }

  def convert(value: Value, toType: Class[_ <: Value]): Option[Value] =
    sanityAndIdentityConverter orElse localConverter orElse ({
      case _ =>
        log.warn("Unable to convert value to type <{}>", toType)
        None
    })((value, toType))

  protected def localConverter: converter = {}
}







case object Num extends DataType(classOf[Value.Num]) {
    val localConverter: converter = {
      case (num: Value.Num, `V_NumClass`) => Some(num)
      case (num: Value.Num, `V_TextClass`) => Some(Value.Text(num.value.toString))
      case (num: Value.Num, `V_RatioClass`) => Some(Value.Ratio(num.value, 1))
      case (num: Value.Num, `V_DayMonthYearClass`) => Some(Value.DayMonthYear(new LocalDate(num.value.toLong)))
    }
  }


trait ValueConverter {
  type converter = PartialFunction[Class[_ <: Value], Option[Value]]

  def sanityAndIdentityConverter(): converter = {
    case (value, _) if (value.getClass != valueType) =>
      log.warn("Incompatible input type got: <{}> expected <{}>", value.getClass, valueType)
      None
    case (value, `valueType`) => Some(value)
  }

  val numConverter =

  def convert(value:Value, fromType:Class[_ <: DataType], toType:Class[_ <: DataType]):Option[Value] = fromType match {
    case V_NumClass => toType match {
      case V_NumClass => Some(value)

    }
  }
}


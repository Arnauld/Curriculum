package curriculum.util

object Misc {

  def someOrNone[T](value:T) = if(value==null) None else Some(value)
}
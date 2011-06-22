package curriculum.util

object Strings {
  def leftPad(value: CharSequence, size: Int, c: Char): String = {
    if (value.length >= size)
      value.toString
    else {
      val builder = new StringBuilder(size)
      value.length.to(size).foreach {
        i =>
          builder.append(c)
      }
      builder.append(value)
      builder.toString
    }
  }

  def extensionOf(path: String) = path.lastIndexOf('.') match {
    case x if (x >= 0) => path.substring(x + 1)
    case _ => ""
  }
}
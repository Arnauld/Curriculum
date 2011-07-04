package curriculum.util

trait AsyncResult[+T] {
  def getResult:Option[T]
}
package curriculum.util

trait AsyncResult[+T] {

  /**
   * Return the result if available
   */
  def getResult:Option[T]

  /**
   * define the callback that will be called once result is available
   */
  def setCallback(c:(T)=>Any)
}
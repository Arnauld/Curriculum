package curriculum.eav

import java.lang.Boolean
import org.slf4j.LoggerFactory

/**
 * The base class for an... instance!
 */
class Instance(val id:Option[Long], val entity:Entity) extends HasId {

  val log = LoggerFactory.getLogger(classOf[Instance])

  /**
   * attributes values
   */
  private var valuesMap = Map[String,Any]()

  /**
   * apply for get
   * @see #getAttributeValue
   */
  def apply(attributeName:String) = getAttributeValue(attributeName)

  /**
   * apply for set
   * @see #setAttributeValue
   */
  def apply(attributeName:String, value:Any) {
    setAttributeValue(attributeName, value)
  }

  def setAttributeValue(nameValuePair:(String, Any)) {
    valuesMap += nameValuePair
  }
  
  def setAttributeValue(attributeName:String, value:Any) {
    valuesMap += (attributeName -> value)
  }

  def getAttributeValue(attributeName:String) = valuesMap.get(attributeName) match {
    case None => // no value found, rely on default value if any
      entity.getAttribute(attributeName) match {
        case Some(attribute) => attribute.defaultValue
        case None => None
      }
    case some => some
  }

  /**
   * Return a view of all attributes values at this point.
   */
  def getAttributeValues = valuesMap

  /**
   * Indicates if this instance has the same attributes as the given one.
   * @return true if all attributes values are the same
   */
  def isMatching(other:Instance):Boolean = {
    log.debug("Is instance matching: {}", other)
    other.getAttributeValues.foldLeft(true)({
      (p,t) =>
        log.debug("Comparing attribute <{}>", t._1);
        p && valuesMap.contains(t._1) && areValuesIdentical(t._2, valuesMap(t._1))
    })
  }

  def areValuesIdentical(v1:Any, v2:Any):Boolean = {
    val res = (v1 == v2)
    log.debug("Are values identicals {} vs {}: " + res, v1, v2)
    res
  }
}









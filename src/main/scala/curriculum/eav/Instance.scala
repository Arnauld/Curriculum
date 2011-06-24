package curriculum.eav

class Instance(val id:Option[Long], val entity:Entity) extends HasId {
  private var valuesMap = Map[String,Any]()

  def apply(attributeName:String) = getAttributeValue(attributeName)
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
  
  def getAttributeValues = valuesMap
}









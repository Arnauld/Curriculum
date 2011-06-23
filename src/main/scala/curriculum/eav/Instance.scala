package curriculum.eav

class Instance(val id:Option[Long], val entity:Entity) extends HasId {
  private var valuesMap = Map[String,Value]()

  def setAttributeValue(attributeName:String, value:Value) {
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









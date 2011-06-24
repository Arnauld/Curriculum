package curriculum.eav

import curriculum.util.HasLabel

object Entity {
  def apply(entityName:String, attributes:Attribute*):Entity = {
    val entity = new Entity(entityName)
    attributes.foreach(entity.declare(_))
    entity
  }
}

class Entity(val entityName:String)  extends HasLabel {

  val entityType = EntityType(entityName)

  val defaultLabel = entityName

  private var attributes = Map[String,Attribute]()

  def declare(attribute:Attribute) {
    attributes += (attribute.attributeName -> attribute)
  }

  def getAttributes = attributes

  def getAttribute(attributeName:String) = attributes.get(attributeName)

  def newInstance:Instance = new Instance(None, this)
  
  def newInstance(attributeValues:(String,Any)*):Instance = {
    val instance = new Instance(None, this)
    attributeValues.foreach(instance.setAttributeValue(_))
    instance
  }
}

object Attribute {
  implicit def tupleToAttribute(t:(String,DataType)):Attribute = new Attribute(t._1, t._2)
}

class Attribute(val attributeName:String,
                val dataType:DataType,
                val defaultValue:Option[Any] = None,
                val upperBound:Int = 1,
                val lowerBound:Int = 0) extends HasLabel {
  val defaultLabel = attributeName
}


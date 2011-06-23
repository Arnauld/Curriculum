package curriculum.eav

object Entity {
  import Attribute._
  def apply(entityName:String, attributes:(String,DataType)*):Entity = {
    val entity = new Entity(entityName)
    attributes.foreach(entity.declare(_))
    entity
  }
}

class Entity(val entityName:String) {

  private var attributes = Map[String,Attribute]()

  def declare(attribute:Attribute) {
    attributes += (attribute.attributeName -> attribute)
  }

  def getAttributes = attributes

  def getAttribute(attributeName:String) = attributes.get(attributeName)

  def newInstance = new Instance(None, this)
}

object Attribute {
  implicit def tupleToAttribute(t:(String,DataType)):Attribute = new Attribute(t._1, t._2)
}

class Attribute(val attributeName:String,
                val dataType:DataType,
                val defaultValue:Option[Value] = None)


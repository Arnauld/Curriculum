package curriculum.eav

import java.lang.Boolean
import org.slf4j.LoggerFactory
import java.io.InputStream
import org.omg.DynamicAny.FieldNameHelper
import service.{EntityNotFoundException, EntityService, InstanceService}
import org.codehaus.jackson.{JsonFactory, JsonToken, JsonParser, JsonGenerator}
import curriculum.util.{UnsupportedDataException, MalformedDataException, ToJSON}

/**
 * The base class for an... instance!
 */
class Instance(val id: Option[Long], val entity: Entity) extends HasId with ToJSON {

  val log = LoggerFactory.getLogger(classOf[Instance])

  /**
   * attributes values
   */
  private var valuesMap = Map[String, Any]()

  /**
   * apply for get
   * @see #getAttributeValue
   */
  def apply(attributeName: String) = getAttributeValue(attributeName)

  /**
   * apply for set
   * @see #setAttributeValue
   */
  def apply(attributeName: String, value: Any) {
    setAttributeValue(attributeName, value)
  }

  def setAttributeValue(nameValuePair: (String, Any)) {
    valuesMap += nameValuePair
  }

  def setAttributeValue(attributeName: String, value: Any) {
    valuesMap += (attributeName -> value)
  }

  def getAttributeValue(attributeName: String) = valuesMap.get(attributeName) match {
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
  def isMatching(other: Instance): Boolean = {
    log.debug("Is instance matching: {}", other)
    other.getAttributeValues.foldLeft(true)({
      (p, t) =>
        log.debug("Comparing attribute <{}>", t._1);
        p && valuesMap.contains(t._1) && areValuesIdentical(t._2, valuesMap(t._1))
    })
  }

  def areValuesIdentical(v1: Any, v2: Any): Boolean = {
    val res = (v1 == v2)
    log.debug("Are values identicals {} vs {}: " + res, v1, v2)
    res
  }

  def writeJSONContent(g: JsonGenerator, ctx: Map[Any, Any]) {
    import curriculum.util.ToJSON._
    writeField("entity", entity.entityName)(g)
    writeField("attributes", valuesMap, ctx)(g)
  }

}

object Instance {
  val log = LoggerFactory.getLogger(classOf[Instance])

  def readFromJSON(jsonAsString: String, instanceService: InstanceService, entityService: EntityService): Option[Instance] = {
    val f = new JsonFactory()
    val jp = f.createJsonParser(jsonAsString)
    readFromJSON(jp, instanceService, entityService)
  }

  def readFromJSON(jp: JsonParser, instanceService: InstanceService, entityService: EntityService): Option[Instance] = {
    if (jp.nextToken() != JsonToken.START_OBJECT) {
      throw new MalformedDataException("Illegal state: instance must start with a <start_object> token")
    }
    else {
      val nextToken = () => {
        val tok = jp.nextToken()
        log.debug("[JSON] token: {}", tok)
        tok
      }
      var instance: Option[Instance] = None
      var attributes = Map.empty[String, Any]
      while (nextToken() != JsonToken.END_OBJECT) {
        val fieldname = jp.getCurrentName
        nextToken() // move to value, or START_OBJECT/START_ARRAY
        log.debug("[JSON] reading field {}", fieldname)
        fieldname match {

          case "entity" =>
            val entityName = jp.getText
            log.debug("[JSON] looking for entity {}", entityName)

            entityService.getEntity(entityName) match {
              case None =>
                throw new EntityNotFoundException("Unknown entity <{}>")
              case Some(e) =>
                instance = Some(e.newInstance)
            }

          case "attributes" =>

            log.debug("[JSON] reading instance attribute for {} (current token {})", instance, jp.getCurrentToken)
            if (jp.getCurrentToken != JsonToken.START_OBJECT) {
              throw new MalformedDataException("Illegal state: attributes must start with a <start_object> token got " + jp.getCurrentToken)
            }

            instance match {

              case None =>
                throw new MalformedDataException("Attributes declaration must occur after entity declaration")

              case Some(inst) =>
                while (nextToken() != JsonToken.END_OBJECT) {
                  val attributeName = jp.getText
                  inst.entity.getAttribute(attributeName) match {

                    case None =>
                      throw new MalformedDataException("Unknown attribute <" + attributeName + "> in entity " + inst.entity.entityName)

                    case Some(a) =>
                      nextToken()
                      val readValue = a.dataType match {
                        case TextType => () => {
                          jp.getText
                        }
                        case EntityType(e) => () => {
                          readFromJSON(jp, instanceService, entityService).get
                        }
                        case x =>
                          throw new UnsupportedDataException("Data type " + x + " is not supported yet")
                      }

                      val attributeValue =
                        if (a.upperBound != 1) {
                          if (nextToken() != JsonToken.START_ARRAY)
                            throw new MalformedDataException("Attribute " + attributeName + " with upper bound different than 1 must have values in array")
                          var values: List[Any] = Nil
                          while (jp.nextToken() != JsonToken.END_ARRAY) {
                            values = readValue() :: values
                          }
                          values
                        }
                        else {
                          readValue
                        }
                      inst.setAttributeValue(attributeName, attributeValue)
                  }
                }
            }

            log.debug("[JSON] reading instance attribute for {} done!", instance)
        }
      }
      instance
    }
  }
}












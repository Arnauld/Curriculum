package curriculum.eav.service

import xml.{Node, NodeSeq}
import curriculum.eav._
import org.slf4j.LoggerFactory

trait InstanceLoader {

  private val logger = LoggerFactory.getLogger(classOf[InstanceLoader])

  def getEntityService: EntityService

  def getInstanceService: InstanceService

  val EntityTypeRef = """([^:]+):([^:]+)""".r

  /**
   *
   * @throws EntityNotFoundException if the instance definition rely on
   *  unknown entity.
   */
  def loadInstance(node: NodeSeq): Instance = {
    val instanceType = (node \ "@type").text
    logger.info("About to load instance of type #{}", instanceType)

    // extract entity name: remove namespace
    val entityName = instanceType match {
      case EntityTypeRef(namespace, eType) => eType
      case withoutNamespace => withoutNamespace
    }

    // lookup corresponding entity or fail
    val entity = getEntityService.getEntity(entityName).getOrElse({
      logger.warn("Entity <{}> not found but referenced by node {}", instanceType, node.toString())
      throw new EntityNotFoundException("Entity <" + instanceType + "> not found")
    })

    // let's go!
    val instance = entity.newInstance
    val attributes = (node \ "attributes" \ "_")
    logger.debug("About to load #{} attributes", attributes.size)
    attributes.foreach({
      e: Node =>
        loadAttribute(instance, entity, e)
    })
    node(0).label match {
      case "instance" =>
        instance
      case "instance-ref" =>
        /*
         * default resolution of instance reference rely on a query by example
         */
        getInstanceService.findUniqueOrNone(instance) match {
          case Some(inst) => inst
          case _ =>
            logger.warn("No instance matching example required by node {}", node.toString())
            throw new InstanceNotFoundException("No instance found")
        }
    }
  }

  private def loadAttribute(instance: Instance, entity: Entity, node: NodeSeq) {
    val attrName = (node \ "@name").text
    entity.getAttribute(attrName) match {
      case None =>
        logger.warn("Unknown attribute {}, it does not belongs to entity {}", attrName, entity.entityName)
      case Some(attr) =>
        logger.debug("Loading attribute <{}>", attrName)
        loadAttributeContent(attr, node, instance, attrName)
    }
  }

  private def loadAttributeContent(attr: Attribute, node: NodeSeq, instance: Instance, attrName: String) {
    val unsupportedType: PartialFunction[(NodeSeq, DataType), Option[Any]] = {
      case _ =>
        logger.warn("Unable to read attribute value of type {}", attr.dataType)
        None
    }
    (simpleParsers orElse parseEntityType(attr) orElse unsupportedType)((node, attr.dataType)) match {
      case Some(value) => instance(attrName, value)
      case None => // no op
    }
  }

  /**
   * Compound all parsers to a single PartialFunction....
   */
  val simpleParsers: PartialFunction[(NodeSeq, DataType), Option[Any]] =
    (parseTextValue :: parseLinkType :: parseHtmlType :: parseDateType :: parseDateRangeType :: parseRatioType :: Nil) reduceLeft (_ orElse _)

  def parseTextValue: PartialFunction[(NodeSeq, DataType), Option[Any]] = {
    case (node: NodeSeq, TextType) => Some(node.text.trim())
  }

  def parseLinkType: PartialFunction[(NodeSeq, DataType), Option[Any]] = {
    case (node: NodeSeq, LinkType) => Some(node.text.trim())
  }

  def parseHtmlType: PartialFunction[(NodeSeq, DataType), Option[Any]] = {
    case (node: NodeSeq, HtmlType) => Some(node \ "_")
  }

  def parseDateType: PartialFunction[(NodeSeq, DataType), Option[Any]] = {
    case (node: NodeSeq, LocalDateType) =>
      val text = node.text.trim()
      LocalDateType.parse(text) match {
        case None =>
          logger.warn("Date value cannot be parsed from <{}>", text)
          None
        case found => found
      }
  }

  def parseDateRangeType: PartialFunction[(NodeSeq, DataType), Option[Any]] = {
    case (node: NodeSeq, LocalDateRangeType) =>
      val text = node.text.trim()
      LocalDateRangeType.parse(text) match {
        case None =>
          logger.warn("Date range value cannot be parsed from <{}>", text)
          None
        case found => found
      }
  }

  def parseRatioType: PartialFunction[(NodeSeq, DataType), Option[Any]] = {
    case (node: NodeSeq, RatioType) =>
      val text = node.text.trim()
      RatioType.parse(text) match {
        case None =>
          logger.warn("Ratio value cannot be parsed from <{}>", text)
          None
        case found => found
      }
  }

  def parseEntityType(attr:Attribute): PartialFunction[(NodeSeq, DataType), Option[Any]] = {
    case (node: NodeSeq, EntityType(_)) =>
      val instanceNodes = (node \ "_")
      val instanceList = instanceNodes.map({
        n => loadInstance(n)
      })
      if (attr.upperBound != 1)
        Some(instanceList)
      else if (instanceList.size == 1)
        Some(instanceList(0))
      else {
        logger.warn("Zero or more than one value found {}, value will be dismissed", instanceList.size)
        None
      }
  }

}




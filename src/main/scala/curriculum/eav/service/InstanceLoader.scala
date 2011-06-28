package curriculum.eav.service

import xml.{Node, NodeSeq}
import curriculum.eav._
import org.slf4j.Logger
import org.joda.time.{LocalDate}

trait InstanceLoader {

  def log: Logger

  def getEntityService: EntityService

  def getInstanceService: InstanceService

  val EntityTypeRef = """([^:]+):([^:]+)""".r

  def loadInstance(node: NodeSeq): Instance = {
    val instanceType = (node \ "@type").text
    log.info("About to load instance of type #{}", instanceType)

    // extract entity name: remove namespace
    val entityName = instanceType match {
      case EntityTypeRef(namespace, eType) => eType
      case withoutNamespace => withoutNamespace
    }

    // lookup corresponding entity or fail
    val entity = getEntityService.getEntity(entityName).getOrElse({
      log.warn("Entity <{}> not found but referenced by node {}", instanceType, node.toString)
      throw new EntityNotFoundException("Entity <" + instanceType + "> not found")
    })
    
    // let's go!
    val instance = entity.newInstance
    val attributes = (node \ "attributes" \ "_")
    log.info("About to load #{} attributes", attributes.size)
    attributes.foreach({
      e: Node =>
        loadAttribute(instance, entity, e)
    })
    node(0).label match {
      case "instance" =>
        instance
      case "instance-ref" =>
        getInstanceService.findUniqueOrNone(instance) match {
          case Some(inst) => inst
          case _ =>
            log.warn("No instance matching example required by node {}", node.toString)
            throw new InstanceNotFoundException("No instance found")
        }
    }


  }

  private def loadAttribute(instance: Instance, entity: Entity, node: NodeSeq) {
    val attrName = (node \ "@name").text
    entity.getAttribute(attrName) match {
      case None =>
        log.warn("Unknown attribute {}, it does not belongs to entity {}", attrName, entity.entityName)
      case Some(attr) =>
        log.info("Loading attribute <{}>", attrName)
        loadAttributeContent(attr, node, instance, attrName)
    }
  }

  private def loadAttributeContent(attr: Attribute, node: NodeSeq, instance: Instance, attrName: String) {
    var ok = true
    val attrType = attr.dataType
    val value = attrType match {
      case TextType =>
        node.text
      case LinkType =>
        node.text.trim()
      case HtmlType =>
        (node \ "_")
      case LocalDateType =>
        parseDate(node.text.trim())
      case LocalDateRangeType =>
         parseDateRange(node.text.trim())
      case RatioType =>
        parseRatio(node.text.trim())
      case EntityType(_) =>
        val instanceNodes = (node \ "_")
        val instanceList = instanceNodes.map({
          n => loadInstance(n)
        })
        if (attr.upperBound != 1)
          instanceList
        else if (instanceList.size == 1)
          instanceList(0)
        else {
          log.warn("Zero or more than one value found {}, value will be dismissed", instanceList.size)
          ok = false
        }
      case _ =>
        log.warn("Unable to read attribute value of type {}", attr.dataType)
        ok = false
    }
    if (ok)
      instance(attrName, value)
  }

  val RatioRegex = """(\d+)/(\d+)""".r

  def parseRatio(value: String): Ratio = {
    val RatioRegex(numerator, denominator) = value
    Ratio(numerator.toInt, denominator.toInt)
  }

  val DateRegex = """([\d]{4})/([\d]{2})/([\d]{2})""".r

  def parseDate(value: String): LocalDate = {
    val DateRegex(year, month, day) = value
    new LocalDate(year.toInt, month.toInt, day.toInt)
  }

  def parseDateRange(value: String): LocalDateRange = {
    val parts = value.split('-')
    val dates = parts.map (parseDate(_))
    // assume if there is one part, it is the min
    dates.size match {
      case 0 => LocalDateRange(null,null)
      case 1 => LocalDateRange(dates(0),null)
      case _ => LocalDateRange(dates(0),dates(1))
    }
  }
}




package curriculum.eav.service

import xml.{Node, NodeSeq}
import curriculum.eav._
import curriculum.util.{HasHtmlDescription, Locales, HasLabel}
import org.slf4j.{LoggerFactory}

trait ModelLoader {

  private val logger = LoggerFactory.getLogger(classOf[ModelLoader])

  def getEntityService: EntityService

  val EntityTypeRef = """([^:]+):([^:]+)""".r

  def load(node: NodeSeq) {
    logger.info("Input entries: [{}] ", (node \ "_").map(_.label).mkString(", "))
    val entities = (node \ "entities" \ "_")
    logger.info("About to load #{} entities", entities.size)
    entities.foreach({
      e: Node =>
        val name = (e \ "@name").text
        logger.info("Loading entity <{}>", name)
        val entity = Entity(name)
        (e \ "attributes" \ "attribute").foreach({
          a: Node =>
            loadAttribute(a, entity.declare(_))
        })
        getEntityService.declare(entity)
    })
  }

  def toDataType(value: String): DataType = value match {
    case "dt:text" => TextType
    case "dt:html" => HtmlType
    case "dt:date" => LocalDateType
    case "dt:date_range" => LocalDateRangeType
    case "dt:ratio" => RatioType
    case "dt:link" => LinkType
    case EntityTypeRef(namespace, name) => getEntityService.getEntity(name) match {
      case Some(e: Entity) => EntityType(e.entityName)
      case None => throw new EntityServiceException("Reference to missing entity <" + name + ">")
    }
    case unknown => throw new EntityServiceException("Unsupported data type <" + unknown + ">")
  }

  def loadAttribute(a: Node, c: (Attribute) => Any) {
    val attName = (a \ "@name").text
    val attType = toDataType((a \ "@type").text)
    val upperBoundNode = (a \ "@upper_bound")
    val upperBound = upperBoundNode.size match {
      case 0 => 1
      case _ => Integer.parseInt(upperBoundNode.text)
    }
    logger.info("Loading attribute <{}> of type <{}>", attName, attType)

    val attribute = new Attribute(attName, attType, None, upperBound)
    loadLabels(a, attribute)
    loadHtmlDescriptions(a, attribute)
    c(attribute)
  }

  def loadLabels(n: Node, dst: HasLabel) {
    (n \ "labels" \ "label").foreach({
      l: Node =>
        val locale = (l \ "@locale").text
        val value = l.text
        dst.setLabel(Locales.toLocale(locale), value)
    })
  }

  def loadHtmlDescriptions(n: Node, dst: HasHtmlDescription) {
    val htmlDescriptions = (n \ "descriptions").filter(_.attribute("type") match {
      case None => false
      case Some(n) => n.text == "html"
    })

    logger.info("Loading #{} html description", htmlDescriptions.size)

    (htmlDescriptions \ "description").foreach({
      l: Node =>
        val locale = (l \ "@locale").text
        val value = (l \ "_") match {
          case NodeSeq.Empty => scala.xml.Text(l.text.trim())
          case nodes => nodes
        }
        logger.debug("Loading description <{}>/<{}>", locale, value.text)
        dst.setHtmlDescription(Locales.toLocale(locale), value)
    })
  }
}




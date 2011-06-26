package curriculum.eav.service

import xml.{Node, NodeSeq}
import curriculum.eav._
import curriculum.util.{Locales, HasLabel}
import org.slf4j.Logger
import com.sun.tools.javac.util.Log
import javax.management.remote.rmi._RMIConnection_Stub

trait ModelLoader {

  def log: Logger

  def getEntityService: EntityService

  val EntityTypeRef = """([^:]+):([^:]+)""".r

  def load(node: NodeSeq) {
    log.info("Input entries: [{}] ", (node \ "_").map(_.label).mkString(", "))
    val entities = (node \ "entities" \ "_")
    log.info("About to load #{} entities", entities.size)
    entities.foreach({
      e: Node =>
        val name = (e \ "@name").text
        log.info("Loading entity <{}>", name)
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
    log.info("Loading attribute <{}> of type <{}>", attName, attType)

    val attribute = new Attribute(attName, attType, None, upperBound)
    loadLabels(a, attribute)
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
}




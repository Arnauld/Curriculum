package curriculum.web.page

import xml.NodeSeq
import org.slf4j.LoggerFactory
import java.util.Locale
import curriculum.eav.{HtmlType, LinkType, Attribute, Instance}

private class InstanceTemplate

object InstanceTemplate {
  val log = LoggerFactory.getLogger(classOf[InstanceTemplate])

  //
  def renderInstance(opt: Option[Any], c: (Instance) => NodeSeq): NodeSeq = {
    log.debug("Attempt to render from <{}>", opt)
    opt match {
      case Some(inst: Instance) =>
        log.debug("Rendering instance of type <{}>", inst.entity.entityName)
        c(inst)
      case _ =>
        log.debug("Rendering instance skipped since it is not an instance of something")
        NodeSeq.Empty
    }
  }

  def renderList(opt: Option[Any], c: (List[Instance]) => NodeSeq): NodeSeq =
    opt match {
      case Some(list: List[Instance]) =>
        c(list)
      case _ =>
        NodeSeq.Empty
    }

  def renderAsFieldset(instance: Instance, attributeNames: List[String], locale: Locale): NodeSeq =
    <fieldset>
      <legend>
        {instance.entity.getLabel(locale)}
      </legend>{val entityName = instance.entity.entityName
    attributeNames.foldLeft(NodeSeq.Empty)({
      (ns, attributeName) =>
        instance.entity.getAttribute(attributeName) match {
          case Some(attribute) =>
            ns ++ renderAttributeAsLabelInput(instance, attribute, locale)
          case _ =>
            log.warn("Unknown attribute <{}> on entity <{}>", attributeName, entityName)
            NodeSeq.Empty
        }
    })}
    </fieldset>

  def renderAttributeAsLabelInput(instance: Instance, attribute: Attribute, locale: Locale): NodeSeq = {
    val entityName = instance.entity.entityName
    val attributeName = attribute.attributeName
    val attributeValue = instance(attributeName) match {
      case None =>
        log.info("No value for attribute")
        ""
      case Some(any) =>
        any.toString.trim()
    }
    <div class={"entry " + attributeName}>
      <label for={entityName + "-" + attributeName} class={entityName + "-" + attributeName}>
        {attribute.getLabel(locale)}
      </label>
        <input name={entityName + "-" + attributeName}
               readonly="true"
               type="text"
               class={entityName + "-" + attributeName} value={attributeValue}/>
    </div>
  }

  def renderAttributeAsLabels(instance: Instance, attribute: Attribute, locale: Locale): NodeSeq = {
    val entityName = instance.entity.entityName
    val attributeName = attribute.attributeName
    val attributeValue = instance(attributeName) match {
      case None =>
        log.info("No value for attribute")
        ""
      case Some(any) =>
        any.toString.trim()
    }
    <div class={"entry " + attributeName}>
      <label for={entityName + "-" + attributeName} class={entityName + "-" + attributeName}>
        {attribute.getLabel(locale)}
      </label>
      {renderAttributeValueAsLabel(instance, attribute, locale)}
    </div>
  }

  def renderAttributeValueAsLabel(instance: Instance, attribute: Attribute, locale: Locale): NodeSeq = {
    val entityName = instance.entity.entityName
    val attributeName = attribute.attributeName
    val attributeValue = instance(attributeName)
    attribute.dataType match {
      case LinkType =>
        <a class={entityName + "-" + attributeName + "-value" + " field-value"}
           href={attributeValue.getOrElse("#").toString}>
          {attributeValue.getOrElse("").toString}
        </a>
      case HtmlType =>
        <div class={entityName + "-" + attributeName + "-value" + " field-value"}>
          {attributeValue.getOrElse(NodeSeq.Empty).asInstanceOf[NodeSeq]}
        </div>
      case _ =>
        <span class={entityName + "-" + attributeName + "-value" + " field-value"}>
          {attributeValue.getOrElse("").toString}
        </span>
    }
  }
}
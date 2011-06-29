package curriculum.domain.web

import curriculum.web.page.HtmlPage
import curriculum.web.page.InstanceTemplate._
import xml.NodeSeq
import java.util.Locale
import org.slf4j.LoggerFactory
import curriculum.eav.{LocalDateRange, Ratio, Attribute, Instance}
import org.joda.time.LocalDate

class CurriculumVitaePage(val instance: Instance, val locale: Locale = Locale.FRANCE) extends HtmlPage {

  val log = LoggerFactory.getLogger(classOf[CurriculumVitaePage])

  declareStylesheets("resources/ui-progress-bar/stylesheets/ui.progress-bar.css")
  declareScripts("resources/ui-progress-bar/javascripts/progress.js")

  bodyContent =
    renderInstance(instance("civility"), renderCivility(_)) ++
      renderInstance(instance("summary"), renderSummary(_)) ++
      renderList(instance("experiences"), renderExperiences(_)) ++
      renderList(instance("skills"), renderSkills(_)) ++
      renderList(instance("interests"), renderInterests(_))

  def entityName = instance.entity.entityName

  def renderCivility(inst: Instance): NodeSeq = {
    val cvilityAttribute = instance.entity.getAttribute("civility").get
    <fieldset id="experiences">
      <legend>
        {cvilityAttribute.getLabel(locale)}
      </legend>{//
      List("first_name", //
        "last_name", //
        "email", //
        "twitter", //
        "blog", //
        "github",
        "librarything", //
        "date_of_birth")
        .foldLeft(NodeSeq.Empty)({
        (ns, attributeName) =>
          inst.entity.getAttribute(attributeName) match {
            case Some(attribute) =>
              ns ++ renderAttributeAsLabels(inst, attribute, locale)
            case _ =>
              log.warn("Unknown attribute <{}> on entity <{}>", attributeName, inst.entity.entityName)
              ns
          }
      })}
    </fieldset>
  }

  def renderSummary(value: Instance): NodeSeq = NodeSeq.Empty

  def sectionSeparator = <p style="text-align: center;">
    <img alt="separateur" src="resources/images/separateur.png" height="20" width="500"/>
    </p>

  def renderExperiences(experiences: List[Instance]): NodeSeq = {
    val attribute = instance.entity.getAttribute("experiences").get
    <fieldset id="experiences">
      <legend>
        {attribute.getLabel(locale)}
      </legend>{//
      // reverse the order of experience: most recent first
      val generated = experiences.reverse.foldLeft(NodeSeq.Empty)(
        (seq: NodeSeq, inst) => seq ++ sectionSeparator ++ renderExperience(attribute, inst))
      // remove first separator
      if (generated.size > 0)
        generated.slice(1, generated.size)
      else
        generated}
    </fieldset>
  }

  def renderExperience(attr: Attribute, xp: Instance): NodeSeq = {
    <section class="experience">
      <header>
        <h1>{xp("title").getOrElse("").toString.trim()}</h1>
        <div class="experience-period">
          {//
          val dr = xp("date_range")
          dr match {
            case None => ""
            case Some(r: LocalDateRange) =>
              val min = if (r.min.isDefined) formatYearMonth(r.min.get) else ""
              if (r.max.isDefined)
                min + " - " + formatYearMonth(r.max.get)
              else
                "Depuis " + min
          }}
        </div>
        <div class="experience-company">
          {xp("company") match {
          case None => ""
          case Some(name: String) =>
            xp("company_website") match {
              case Some(site: String) =>
                <span>
                  <a href={site}>{name}</a>
                </span>
              case None => <span>
                {name}
              </span>
            }
        }}
        </div>
      </header>
      <div class="experience-description">
        {xp("description").getOrElse(NodeSeq.Empty).asInstanceOf[NodeSeq]}
      </div>
    </section>
  }

  def formatYearMonth(date:LocalDate) = date.toString("MMM YYYY", locale)

  def renderSkills(skills: List[Instance]): NodeSeq = {
    val attribute = instance.entity.getAttribute("skills").get
    <fieldset id="skills">
      <legend>
        {attribute.getLabel(locale)}
      </legend>
      <div class="description">
        {attribute.getHtmlDescription(locale)}
      </div>
      <table>
        {skills.foldLeft(NodeSeq.Empty)(
        (seq: NodeSeq, inst) => seq ++ renderSkill(attribute, inst))}
      </table>
    </fieldset>
  }

  def renderSkill(attr: Attribute, skill: Instance): NodeSeq = {
    val skillName = skill("title").getOrElse("")
    val skillLevel = skill("level").getOrElse(Ratio(50)).asInstanceOf[Ratio].toPercent
    <tr class="skill">
      <td class="field-label skill-name">
        {skillName}
      </td>
      <td>
        <div class="field-value">
          <div class="ui-progress-bar ui-container">
            <div class="ui-progress" style={"width: " + skillLevel + "%;"}>
              {skill("level_comment") match {
              case None => NodeSeq.Empty
              case Some(c) => <span class="ui-label">
                {c}
              </span>
            }}
            </div>
          </div>
        </div>
      </td>
      <td>
        {skill("comment") match {
        case None => NodeSeq.Empty
        case Some(c) => <span class="skill-comment">
          {c}
        </span>
      }}
      </td>
    </tr>
  }

  def renderInterests(interests: List[Instance]): NodeSeq = {
    val attribute = instance.entity.getAttribute("interests").get
    <fieldset id="interests">
      <legend>
        {attribute.getLabel(locale)}
      </legend>
      <div class="description">
        {attribute.getHtmlDescription(locale)}
      </div>
      <table>
        {interests.foldLeft(NodeSeq.Empty)(
        (seq: NodeSeq, inst) => seq ++ renderInterest(attribute, inst))}
      </table>
    </fieldset>
  }

  def renderInterest(attr: Attribute, interest: Instance): NodeSeq = {
    val interestName = interest("title").getOrElse("")
    val interestLevel = interest("level").getOrElse(Ratio(50)).asInstanceOf[Ratio].toPercent
    <tr class="interest">
      <td class="field-label interest-name">
        {interestName}
      </td>
      <td>
        <div class="field-value">
          <div class="ui-progress-bar ui-container">
            <div class="ui-progress" style={"width: " + interestLevel + "%;"}>
              {interest("level_comment") match {
              case None => NodeSeq.Empty
              case Some(c) => <span class="ui-label">
                {c}
              </span>
            }}
            </div>
          </div>
        </div>
      </td>
      <td>
        {interest("comment") match {
        case None => NodeSeq.Empty
        case Some(c) => <span class="interest-comment">
          {c}
        </span>
      }}
      </td>
    </tr>
  }
}

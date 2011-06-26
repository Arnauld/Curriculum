package curriculum.domain.web

import curriculum.eav.Instance
import curriculum.web.page.HtmlPage
import curriculum.web.page.InstanceTemplate._
import xml.NodeSeq

class CurriculumVitaePage(val instance: Instance) extends HtmlPage {
  declareStylesheets("resources/ui-progress-bar/stylesheets/ui.progress-bar.css")
  declareScripts("ui-progress-bar/javascripts/progress.js")

  bodyContent = renderInstance(instance("civility"), renderCivility(_)) ++
    renderInstance(instance("summary"), renderSummary(_)) ++
    renderList(instance("experiences"), renderExperience(_)) ++
    renderList(instance("skills"), renderSkill(_)) ++
    renderList(instance("interests"), renderInterest(_))

  def renderCivility(value: Instance): NodeSeq = NodeSeq.Empty

  def renderSummary(value: Instance): NodeSeq = NodeSeq.Empty

  def renderExperience(value: Instance): NodeSeq = NodeSeq.Empty

  def renderSkill(value: Instance): NodeSeq = NodeSeq.Empty

  def renderInterest(value: Instance): NodeSeq = NodeSeq.Empty

}
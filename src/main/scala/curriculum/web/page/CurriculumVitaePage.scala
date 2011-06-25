package curriculum.web.page

import curriculum.web.page.InstanceTemplate._
import curriculum.eav.Instance
import xml.NodeSeq

class CurriculumVitaePage(val instance: Instance) extends HtmlPage {
  declareStylesheets("resources/ui-progress-bar/stylesheets/ui.progress-bar.css")
  declareScripts("ui-progress-bar/javascripts/progress.js")

  bodyContent = renderInstance(instance("civility"), renderCivility(_)) ++
    renderInstance(instance("summary"), renderSummary(_)) ++
    renderInstance(instance("experience"), renderExperience(_)) ++
    renderInstance(instance("skills"), renderSkills(_)) ++
    renderInstance(instance("interests"), renderInterests(_))

  def renderCivility(value: Instance): NodeSeq = NodeSeq.Empty

  def renderSummary(value: Instance): NodeSeq = NodeSeq.Empty

  def renderExperience(value: Instance): NodeSeq = NodeSeq.Empty

  def renderSkills(value: Instance): NodeSeq = NodeSeq.Empty

  def renderInterests(value: Instance): NodeSeq = NodeSeq.Empty

}
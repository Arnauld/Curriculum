package curriculum.web.page

import curriculum.eav.Instance
import xml.NodeSeq

object InstanceTemplate {
  //
  def renderInstance(opt: Option[Any], c: (Instance) => NodeSeq): NodeSeq =
    opt match {
      case Some(inst: Instance) => c(inst)
      case _ => NodeSeq.Empty
    }
}
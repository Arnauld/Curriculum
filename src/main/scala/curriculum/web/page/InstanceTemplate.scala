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

  def renderList(opt: Option[Any], c: (Instance) => NodeSeq): NodeSeq =
    opt match {
      case Some(list: List[_]) => list.foldLeft(NodeSeq.Empty)(
        (seq:NodeSeq, inst) => seq ++ renderInstance(Some(inst), c))
      case _ => NodeSeq.Empty
    }
}
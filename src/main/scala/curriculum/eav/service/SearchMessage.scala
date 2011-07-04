package curriculum.eav.service

import curriculum.message.Message
import curriculum.eav.Instance

object SearchMessage {

  object Type {

    case object NoSimilarInstanceDefined extends Message.Type("type-no-similar-instance-defined")

    case object SearchBySimilitude extends Message.Type("type-search-by-similitude")

    case object SearchFinished extends Message.Type("type-search-finished")

  }

  def noSimilarInstanceDefined() = {
    Message(Type.NoSimilarInstanceDefined,
      Message.code("Aucun critère de recherche n'est défini"))
  }

  def searchBySimilitude(instance:Instance, keywords:Array[String]) = {
    Message(Type.SearchBySimilitude,
      Message.code("Recherche par similitude déclenchée"))
  }

  def searchFinished(nbResult:Int) = {
    Message(Type.SearchBySimilitude,
      Message.code("Recherche terminée: %d résultat(s)"), nbResult)
  }
}








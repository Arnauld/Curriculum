package curriculum.eav.service

import curriculum.message.Message
import curriculum.message.Message._

object SearchServiceMessage {

  object Type {

    case object InstanceFound extends Message.Type("type-search-instance-found")

  }

  def instanceFound(instance:WeightedInstance) = {
    Message(Type.InstanceFound,
      Message.code("Instance trouvée %s"))
  }
}
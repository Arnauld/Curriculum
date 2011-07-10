package curriculum.eav.service.web

import curriculum.message.Message
import curriculum.eav.Instance
import curriculum.eav.service.WeightedInstance

object WebMessage {

  object Type {

    case object InstanceLink extends Message.Type("type-instance-link")

  }

  def weightedInstanceLink(instance: WeightedInstance, baseURL: String) = {
    Message(Type.InstanceLink,
      Message.code(
        <span>Instance
          <a href={baseURL.format(instance.instanceId)} class="instance-link">#%d</a>
          pertinance %d%%</span>.toString()),
      instance.instanceId,
      instance.weight)
  }
}

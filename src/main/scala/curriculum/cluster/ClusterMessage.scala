package curriculum.cluster

import curriculum.util.Message

object ClusterMessage {
  object Type {
    case object NodeStarting extends Message.Type("type-node-starting")
    case object NodeRunning extends Message.Type("type-node-running")
  }
  def nodeStarting(node:ClusterNode) = {
    Message(Type.NodeStarting, Message.code("Démarrage du noeud %s sur %s:%d"), node.name, node.address, node.port)
  }
}
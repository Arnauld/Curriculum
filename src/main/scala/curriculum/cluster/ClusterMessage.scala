package curriculum.cluster

import curriculum.message.Message

object ClusterMessage {

  object Type {

    case object NoNodeRunning extends Message.Type("type-no-node-running")

    case object NodeStarting extends Message.Type("type-node-starting")

    case object NodeRunning extends Message.Type("type-node-running")

    case object NodeStarted extends Message.Type("type-node-started")

    case object NodeAlreadyStarted extends Message.Type("type-node-already-started")

    case object NodeBindError extends Message.Type("type-node-bind-error")

    case object NodeStartError extends Message.Type("type-node-start-error")

  }

  def nodesRunningWeb(nodes: Iterable[ClusterNode]) = {
    var builder = new StringBuilder("Noeuds en attente de connexion:")
    builder.append("<ul>")
    nodes.foreach({n =>
        builder.append("<li><b>").append(n.name).append("</b>")
               .append(" sur ")
               .append(n.address).append(":").append(n.port).append("</li>")
    })
    builder.append("</ul>")
    Message(Type.NodeRunning, Message.code(builder.toString))
  }

  def noNodeRunning() = {
    Message(Type.NoNodeRunning, Message.code("<b>Aucun noeud disponible</b>. En tant qu'administrateur vous devriez en allumer un!"))
  }

  def nodeStarting(node: ClusterNode) = {
    Message(Type.NodeStarting, Message.code("Démarrage du noeud %s sur %s:%d"), node.name, node.address, node.port)
  }

  def nodeAlreadyStarted(node: ClusterNode) = {
    Message(Type.NodeAlreadyStarted, Message.code("Noeud %s déjà démarré sur %s:%d"), node.name, node.address, node.port)
  }

  def nodeStarted(node: ClusterNode) = {
    Message(Type.NodeStarted, Message.code("Noeud %s démarré sur %s:%d"), node.name, node.address, node.port)
  }

  def nodeRunning(node: ClusterNode) = {
    Message(Type.NodeRunning, Message.code("Noeud %s en attente de connexion sur %s:%d"), node.name, node.address, node.port)
  }

  def nodeBindError(node: ClusterNode) = {
    Message(Type.NodeBindError, Message.code("Echec lors de démarrage du noeud %s sur %s:%d, port probablement déjà utilisé"), node.name, node.address, node.port)
  }

  def nodeStartError(node: ClusterNode) = {
    Message(Type.NodeStartError, Message.code("Echec lors de démarrage du noeud %s sur %s:%d : consultez le journal"), node.name, node.address, node.port)
  }

}
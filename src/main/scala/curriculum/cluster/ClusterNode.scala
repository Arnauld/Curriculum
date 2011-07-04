package curriculum.cluster

case class ClusterNode(name:String, port:Int, parameters:Map[String,Any]) {
  def address = parameters.get("address").getOrElse("localhost").asInstanceOf[String]
}
package curriculum.eav.service

import curriculum.eav.Instance
import javax.management.remote.rmi._RMIConnection_Stub

trait InstanceService {
  private var instanceByType = Map[String,List[Instance]]()

  def register(instance:Instance) {
    val entityName = instance.entity.entityName
    val registered = instance :: instanceByType.getOrElse(entityName, {Nil})
    instanceByType += (entityName -> registered)
  }

  def findUniqueOrNone(example:Instance):Option[Instance] =
    find(example) match {
      case elem :: Nil => Some(elem)
      case _ => None
    }

  def find(example:Instance):List[Instance] =
    instanceByType.get(example.entity.entityName) match {
      case None => List()
      case Some(list) => list.filter({_.isMatching(example)})
    }

}

class InstanceServiceException(message: String, cause: Throwable) extends Exception(message, cause) {
  def this(message: String) = this (message, null)
}

class InstanceNotFoundException(message: String, cause: Throwable) extends InstanceServiceException(message, cause) {
  def this(message: String) = this (message, null)
}
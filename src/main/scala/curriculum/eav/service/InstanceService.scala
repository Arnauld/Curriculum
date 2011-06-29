package curriculum.eav.service

import curriculum.eav.Instance

trait InstanceService {
  /**
   * acts as simple repository
   */
  private var instanceByType = Map[String, List[Instance]]()

  /**
   * Register instance in the underlying repository
   */
  def register(instance: Instance) {
    val entityName = instance.entity.entityName
    val registered = instance :: instanceByType.getOrElse(entityName, {
      Nil
    })
    instanceByType += (entityName -> registered)
  }

  /**
   * Query by example
   *
   */
  def findUniqueOrNone(example: Instance): Option[Instance] =
    find(example) match {
      case elem :: Nil => Some(elem)
      case _ => None
    }

  /**
   * Query by example
   *
   */
  def find(example: Instance): List[Instance] =
    instanceByType.get(example.entity.entityName) match {
      case None => List()
      case Some(list) => list.filter({
        _.isMatching(example)
      })
    }

}

class InstanceServiceException(message: String, cause: Throwable) extends Exception(message, cause) {
  def this(message: String) = this (message, null)
}

class InstanceNotFoundException(message: String, cause: Throwable) extends InstanceServiceException(message, cause) {
  def this(message: String) = this (message, null)
}
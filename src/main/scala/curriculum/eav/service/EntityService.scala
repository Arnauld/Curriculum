package curriculum.eav.service

import curriculum.eav._

trait EntityService {

  private var entities = Map[String, Entity]()

  def declare(entity: Entity) {
    entities += (entity.entityName -> entity)
  }

  def getEntity(entityName: String) = entities.get(entityName)

  def getEntities = entities
}

class EntityServiceException(message: String, cause: Throwable) extends Exception(message, cause) {
  def this(message: String) = this (message, null)
}

class EntityNotFoundException(message: String, cause: Throwable) extends EntityServiceException(message, cause) {
  def this(message: String) = this (message, null)
}
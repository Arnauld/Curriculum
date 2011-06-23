package curriculum.eav.service

import curriculum.eav.{Instance, Entity}

trait EntityService {

  private var entities = Map[String,Entity]()
  def declare(entity:Entity) {
    entities += (entity.entityName -> entity)
  }

  def getEntity(entityName:String) = entities(entityName)
}
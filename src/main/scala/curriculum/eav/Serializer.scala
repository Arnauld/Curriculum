package curriculum.eav

import service.EntityService
import java.io._

trait Serializer {
  def writeInstance(instance:Instance, out:DataOutputStream) {
    val oout = new ObjectOutputStream(out)
    oout.writeUTF(instance.entity.entityName)
    oout.writeObject(instance.getAttributeValues)
  }
  def readInstance(entityService:EntityService, in:DataInputStream):Option[Instance] = {
    val oin = new ObjectInputStream(in)
    val entityName = oin.readUTF()
    val attributes = oin.readObject()
    entityService.getEntity(entityName) match {
      case None => None
      case Some(e) =>
        val inst = e.newInstance
        attributes.asInstanceOf[Map[String,Any]].foreach({t =>
          inst.setAttributeValue(t)
        })
        Some(inst)
    }
  }
}

trait SerializerComponent {
  def serializer:Serializer = new Serializer {}
}
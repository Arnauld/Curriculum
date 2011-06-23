package curriculum.eav

import java.util.concurrent.atomic.AtomicLong

class RepositoryException(message: String, cause: Throwable) extends Exception(message, cause) {
  def this(message: String) = this (message, null)
}

trait Repository[A <: HasId] {
  def save(instance: A): A
  def delete(id: Long)
  def load(id: Long): Option[A]
}

trait Persistent[A <: HasId] {
  self: {
    def log: org.slf4j.Logger
  } =>

  def getPersistent:A

  def load(implicit repository: Repository[A]): Option[A] =
    getPersistent.id match {
      case None =>
        log.warn("Attempt to load a {} but no id is provided", getClass.getSimpleName)
        None
      case Some(idValue) =>
        repository.load(idValue)
    }

  def delete(implicit repository: Repository[A]) {
    getPersistent.id match {
      case None =>
        log.warn("Attempt to delete a {} but no id is provided", getClass.getSimpleName)
      case Some(idValue) =>
        repository.delete(idValue)
    }
  }

  def save(implicit repository: Repository[A]): A = repository.save(getPersistent)
}


trait HasId {
  def id:Option[Long]
}

trait IdGenerator {
  def nextId:Long
}

object IdGenerator {
  implicit var sequence = new Sequence(0L)

  class Sequence(start:Long) extends IdGenerator {
    val generator = new AtomicLong(start)
    def nextId = generator.incrementAndGet()
  }
}
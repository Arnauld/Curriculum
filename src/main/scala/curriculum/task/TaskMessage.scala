package curriculum.task

import curriculum.message.Message

object TaskMessage {
  object Type {
    case object TaskScheduled extends Message.Type("type-task-scheduled")
    case object TaskDone extends Message.Type("type-task-done")
  }
  def taskScheduled(task:Task, details:Any = "") = {
    Message(Type.TaskScheduled, Message.code("Tache %d lancée: %s"), task.taskId, details)
  }
  def taskDone(task:Task, details:Any = "") = {
    Message(Type.TaskDone, Message.code("Tache %d terminée: %s"), task.taskId, details)
  }
}
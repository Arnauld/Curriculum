package curriculum.task

import java.util.concurrent.atomic.AtomicInteger
import curriculum.task.TaskStatus.{Initial, Cancelled, Done, Running}

trait Task {

  def taskFinished_? = taskDone_? || taskCancelled_?

  def taskDone_? = taskStatus==Done

  def taskCancelled_? = taskStatus==Cancelled

  def taskRunning_? = taskStatus==Running

  def taskStatus:TaskStatus

  def taskProgress:Int

  def taskId:Long

  def cancelJob()
}

class SimpleTask(val taskId:Long) extends Task {
  var progressUpperBound = 100
  var progress = new AtomicInteger()
  var status:TaskStatus = Initial
  var startedAt:Option[Long] = None
  var finishedAt:Option[Long] = None
  def workDone(amount:Int = 1) {
    progress.addAndGet(amount)
  }

  def taskFinished() {
    status = status match {
          case Done => Done
          case Initial => Done
          case Running => Done
          case Cancelled => Cancelled
        }
    finishedAt = Some(System.currentTimeMillis())
  }

  def taskStart() {
    status = Running
    startedAt = Some(System.currentTimeMillis())
  }

  def taskStatus = status

  def taskProgress = (100*progress.get)/progressUpperBound

  def cancelJob() {
    status = Cancelled
  }
}

sealed trait TaskStatus
object TaskStatus {
  case object Initial extends TaskStatus
  case object Running extends TaskStatus
  case object Done extends TaskStatus
  case object Cancelled extends TaskStatus
}

package curriculum.task

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{TimeUnit, Executors}
import curriculum.message.MessageQueue
import curriculum.util.{LocaleAware, ProgressMonitor, RunnableWithProgress, DaemonThreadFactory}

trait TaskService {
  private val executor = Executors.newFixedThreadPool(4, new DaemonThreadFactory("TaskService-%d"))
  private val victor = Executors.newScheduledThreadPool(1)

  private var tasks:List[InternalTask] = Nil

  private val idGen = new AtomicLong()

  def start () {
    victor.scheduleAtFixedRate(new Runnable {
      def run() {
        markAndSweep()
      }
    }, 1L, 1L, TimeUnit.MINUTES)
  }

  private[task] def markAndSweep() {
    // keep finished task for 5mins, and let's mark the older ones
    val threshold = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)

    //
    tasks.foreach({job => job.finishedAt match {
      case None => // nothing to do
      case Some(ts) =>
        if(ts<threshold)
          job.marked = true
    }})

    // now the sync' part: sweep
    synchronized {
      tasks = tasks.filterNot(_.marked)
    }
  }

  def spawn(r: RunnableWithProgress, details:LocaleAware): Task = {
    val task = new InternalTask(idGen.incrementAndGet(), r)
    MessageQueue.Local.publish(TaskMessage.taskScheduled(task, details))
    executor.submit(task)
    synchronized {
      tasks = task::tasks
    }
    task
  }

  class InternalTask(id: Long, r: RunnableWithProgress) extends SimpleTask(id) with ProgressMonitor with Runnable {
    private[task] var marked = false

    override def beginTask(name: String, amount: Int) {
      progressUpperBound = amount
    }

    override def worked(amount: Int) {
      workDone(amount)
    }

    override def done() {
      status = TaskStatus.Done
    }

    override def subMonitor = new ProgressMonitor {}

    def run() {
      try {
        taskStart()
        r.run(this)
        done()
      }
      finally {
        taskFinished()
      }
    }
  }

}
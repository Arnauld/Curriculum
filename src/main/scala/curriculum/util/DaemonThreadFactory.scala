package curriculum.util

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import java.lang.Thread

class DaemonThreadFactory(val namePattern:String, val group:ThreadGroup) extends ThreadFactory {

  def this(namePattern:String) = this(namePattern, new ThreadGroup(namePattern))

  def this() = this("DaemonThreadFactory-%d")

  val idGen = new AtomicInteger()

  def newThread(r: Runnable) = {
    val thread = new Thread(group, r, namePattern.format(idGen.incrementAndGet()))
    thread.setDaemon(true)
    thread
  }
}
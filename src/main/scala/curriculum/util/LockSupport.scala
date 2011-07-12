package curriculum.util

import java.util.concurrent.locks.Lock

object LockSupport {

  def withinLock[T](f: =>T)(implicit lock:Lock):T = {
    lock.lock()
    try {
      f
    }
    finally lock.unlock()
  }
}
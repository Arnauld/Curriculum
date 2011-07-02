package curriculum.util

trait ProgressMonitor {
  /**
   * no op by default
   */
  def beginTask(name: String, amount: Int) {}

  /**
   * no op by default
   */
  def subTask(subTaskName: String) {}

  /**
   * no op by default
   */
  def worked(amount: Int) {}

  /**
   * no op by default
   */
  def done() {}

  private var isCancelled:Boolean = false

  def cancel() {
    isCancelled = true
  }

  def cancelled_? = isCancelled

  /**
   * return this by default
   */
  def subMonitor: ProgressMonitor = {
    this
  }
}
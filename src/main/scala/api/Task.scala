package es.elv.kobold.api

trait TaskEvents {
  def onTaskStarted(task: ITask)
  def onTaskCompleted(task: ITask)
  def onTaskCancelled(task: ITask)
}


// Exported to JS.
trait ITask {
  def isCompleted: Boolean

  def isCncelled: Boolean

  def cancel

  def runtime: Long
}

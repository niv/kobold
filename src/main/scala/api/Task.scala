package es.elv.kobold.api

// Exported to JS.
trait ITask {
  def isCompleted: Boolean

  def isCancelled: Boolean

  def cancel

  def runtime: Long
}

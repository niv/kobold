package es.elv.kobold.game

import es.elv.kobold.api._
import es.elv.kobold.host.Host

trait TaskEvents {
  def onTaskStarted(obj: ICreature with IActionable, task: ITask)
  def onTaskCompleted(obj: ICreature with IActionable, task: ITask)
  def onTaskCancelled(obj: ICreature with IActionable, task: ITask)
}

trait BaseTask extends ITask {
  private var _completed = false
  private var _cancelled = false

  private val startedOn = System.currentTimeMillis
  //val stoppedOn: Option[Long]

  def isCompleted: Boolean = _completed
  def isCancelled: Boolean = _cancelled
  def isDead: Boolean = isCancelled || isCompleted

  def runtime: Long = System.currentTimeMillis - startedOn

  def tick

  def complete = if (!isDead) _completed = true
  def cancel = if (!isDead) _cancelled = true
}

class TaskDeadException extends RuntimeException

class TaskManager(private val parent: ICreature with IActionable) {
  private val q = scala.collection.mutable.Queue[BaseTask]()
  private var current: Option[BaseTask] = None

  def clear {
    current match {
      case Some(ta) =>
        ta.cancel
        Host.onTaskCancelled(parent, ta)
        current = None
      case None =>
    }
    for (task <- q) Host.onTaskCancelled(parent, task)
    q.clear
  }

  def <<(task: BaseTask) = add(task)
  def add(task: BaseTask): BaseTask = {
    if(task.isDead) throw new TaskDeadException
    q += task
    schedule
    task
  }

  def tick {
    schedule
    current match {
      case Some(ta) => ta.tick
      case None =>
    }
    schedule
  }

  def taskCount: Int = 0

  def taskList = current match {
    case Some(task) =>
      task :: q.toList
    case None =>
      q.toList
    }

  private def schedule {
    if (current.isEmpty && q.size == 0)
      return

    if (current.isDefined && current.get.isCompleted) {
      Host.onTaskCompleted(parent, current.get)
      current = None
    }

    if (current.isDefined && current.get.isCancelled) {
      Host.onTaskCancelled(parent, current.get)
      current = None
    }

    if (current.isEmpty && q.size > 0) {
      current = Some(q.dequeue)
      Host.onTaskStarted(parent, current.get)
    }
  }
}

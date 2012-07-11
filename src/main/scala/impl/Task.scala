package es.elv.kobold.impl.api

import es.elv.kobold.impl.script._

import es.elv.kobold.api._

trait BaseTask extends ITask {
  private var _completed = false
  private var _cancelled = false
  
  private val startedOn = System.currentTimeMillis
  //val stoppedOn: Option[Long]

  def isCompleted: Boolean = _completed
  def isCancelled: Boolean = _cancelled
  def isDead: Boolean = isCancelled || isCompleted
  
  def runtime: Long = System.currentTimeMillis - startedOn
  
 

  def setup(host: ICreature, va: List[Object])
  def tick

  def complete = if (!isDead) _completed = true
  def cancel = if (!isDead) _cancelled = true
}

class TaskDeadException extends RuntimeException

class TaskManager {
  private val q = scala.collection.mutable.Queue[BaseTask]()
  private var current: Option[BaseTask] = None

  def clear {
    current match {
      case Some(ta) =>
        ta.cancel
        Host.onTaskCancelled(ta)
        current = None
      case None =>
    }
    for (task <- q) Host.onTaskCancelled(task)
    q.clear
  }

  def add(task: BaseTask) {
    if(task.isDead) throw new TaskDeadException
    q += task
    schedule
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

  private def schedule {
    if (current.isEmpty && q.size == 0)
      return
    
    if (current.isDefined && current.get.isCompleted) {
      Host.onTaskCompleted(current.get)
      current = None
    }

    if (current.isDefined && current.get.isCancelled) {
      Host.onTaskCancelled(current.get)
      current = None
    }
    
    if (current.isEmpty && q.size > 0) {
      current = Some(q.dequeue)
      Host.onTaskStarted(current.get)
    }
  }
}

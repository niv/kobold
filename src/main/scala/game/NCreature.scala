package es.elv.kobold.game

import org.nwnx.nwnx2.jvm.{Scheduler, NWObject}
import es.elv.kobold.api._
import es.elv.kobold.{G, N}

import es.elv.kobold.Implicits._

class NCreature(w: NWObject) extends NObject(w) with ICreature {
  lazy val isPlayer = N.getIsPC(this)

  def say(m: String) = <= {
    N.speakString(m, 0)
  }

  def whisper(message: String) = <= {
    N.speakString(message, 1)
  }

  def sees(other: ICreature): Boolean =
    N.getObjectSeen(other, this)

  private def nearObjects[T <: NObject](typeMask: Int, distance: Float): List[T] =
    N.getObjectsInShape(
      4 /* SHAPE_SPHERE */,
      distance,
      N.getLocation(this),
      true /* LOS */,
      typeMask,
      IVector3.ORIGIN).
        map(G[T](_)).
        filter(_ != this) toList

  def getPerceivedCreatures(distance: Float) =
    nearObjects[NCreature](1 /*OBJECT_TYPE_CREATURE*/, distance).
      filter(sees(_))

  def location = N.getLocation(this)

  lazy val taskManager: TaskManager = new TaskManager(this)

  def busy = taskManager.taskList.size > 0

  def taskList = taskManager.taskList
  // def taskCount = taskManager.taskCount

  def clear = taskManager.clear

  def taskFollow(o: ICreature, d: Float): ITask = {
    checkAccess
    taskManager << new FollowTask(this, o, d)
  }
}

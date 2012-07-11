package es.elv.kobold.game

import org.nwnx.nwnx2.jvm.{Scheduler, NWObject, NWScript}
import es.elv.kobold.api._
import es.elv.kobold.G

import es.elv.kobold.Implicits._

class NCreature(w: NWObject) extends NObject(w) with ICreature {
  lazy val isPlayer = NWScript.getIsPC(this)

  def say(m: String) = <= {
    NWScript.speakString(m, 0)
  }

  def whisper(message: String) = <= {
    NWScript.speakString(message, 1)
  }
  
  def sees(other: ICreature): Boolean =
    NWScript.getObjectSeen(other, this)
  
  private def nearObjects[T <: NObject](typeMask: Int, distance: Float): List[T] =
    NWScript.getObjectsInShape(
      4 /* SHAPE_SPHERE */,
      distance,
      NWScript.getLocation(this),
      true /* LOS */,
      typeMask,
      IVector3.ORIGIN) map { G[T](_) } toList

  def getPerceivedCreatures(distance: Float) =
    nearObjects[NCreature](1 /*OBJECT_TYPE_CREATURE*/, distance) filter (sees(_)) toArray

  def location = NWScript.getLocation(this)


  private lazy val taskManager: TaskManager = new TaskManager
 
  //def taskList = 

  def taskList = List()
  def taskCount = taskManager.taskCount
  
  def clear = taskManager.clear

  def taskFollow(o: ICreature, d: Float): ITask = null
}

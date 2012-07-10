package es.elv.kobold.impl.intf

import org.nwnx.nwnx2.jvm.{Scheduler, NWObject, NWScript}
import es.elv.kobold.intf._
import es.elv.kobold.G

import es.elv.kobold.Implicits._

class NCreature(w: NWObject) extends NObject(w) with ICreature {
  lazy val isPlayer = NWScript.getIsPC(this)

  def say(m: String) = assign {
    NWScript.speakString(m, 0)
  }

  def whisper(message: String) = assign {
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
}

package es.elv.kobold.glue

import org.nwnx.nwnx2.jvm._

import es.elv.kobold.{G, GCoreClasses}
import es.elv.kobold.host._
import es.elv.kobold.game._
import es.elv.kobold.api._

import com.codahale.logula.Logging
import org.apache.log4j.Level

private [glue] object EventHandler extends SchedulerListener with Logging {
  def postFlushQueues(remainingTokens: Int) {}
  def missedToken(objSelf: NWObject, token: String) {}
  def context(objSelf: NWObject) {}

  private def e[X <: IObject](self: X)(event: String, va: Object*) =
    Host.handleObjectEvent(self, event, va.toList)
  
  case class E(e: String, o: NWObject)

  def event(objSelf: NWObject, event: String) {
    implicit val self = objSelf
    log.debug("event: " + event + " on " + objSelf)
    E(event, objSelf) match {
      case E("creature_spawn", o: ICreature) =>
        e(o)("creature.spawn")
      
      case E("creature_perc", o: ICreature) =>
        val (heard, seen, inaudible, vanished, last) = (
          NWScript.getLastPerceptionHeard, NWScript.getLastPerceptionSeen,
          NWScript.getLastPerceptionInaudible, NWScript.getLastPerceptionVanished,
          NWScript.getLastPerceived
        )
        require(List(heard, seen, inaudible, vanished).count(_ == true) == 1)
        if (heard) e(o)("creature.hears", last)
        else if (seen) e(o)("creature.sees", last)
        else if (vanished) e(o)("creature.seesnot", last)
        else if (inaudible) e(o)("creature.hearsnot", last)

      case E("creature_hb", o: ICreature) =>
        // Host.onCreatureHB(o) // for task manager!

      case e =>
        log.debug("unhandled: " + e)
    }

    Scheduler.flushQueues
  }
}

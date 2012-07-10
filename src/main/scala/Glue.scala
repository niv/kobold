package es.elv.kobold.glue

import org.nwnx.nwnx2.jvm._

import es.elv.kobold.{G, GCoreClasses}
import es.elv.kobold.impl.script.Host
import es.elv.kobold.impl.intf._
import es.elv.kobold.intf._
import es.elv.kobold.script._

import com.codahale.logula.Logging
import org.apache.log4j.Level

object ObjectHandler extends NWObject.ObjectHandler with Logging {
  def handleObjectClass(obj: NWObject, valid: Boolean, objType: Int,
        resRef: String, tag: String): NWObject =
    G(obj)
}

object EventHandler extends SchedulerListener with Logging {
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

      /*
      case E("mod_load", o: IModule) =>
        e(o)("module.load")
      case E("mod_hb", o: IModule) =>
        e(o)("module.hb")
      */

      case e =>
        log.debug("unhandled: " + e)
    }

    Scheduler.flushQueues
  }
}

object Init extends Logging {
  import es.elv.kobold.lang.rhino._
  
  Logging.configure { log =>
    log.level = Level.DEBUG
    log.console.enabled = true
    log.console.threshold = Level.DEBUG
  }

  def setup {}
  def shutdown {}
  
  val src = io.Source.fromFile("creature.js").mkString
  val rhino = new RhinoImpl
  val script = rhino.prepare(Host, src)

  def init {
	  GCoreClasses.registerAll

    Scheduler addSchedulerListener EventHandler
    NWObject registerObjectHandler ObjectHandler
    //NWObject registerEffectHandler EffectHandler
    //NWObject registerItemPropertyHandler IPropHandler


    // TODO: read all scripts & start up Host
    // Host.registerLanguage(rhino)

    rhino.execute(Host, Module(), script)

    val testObj: IObject = new NCreature(new NWObject(2))
    Host attachContext (script, Set(testObj))
  }
}

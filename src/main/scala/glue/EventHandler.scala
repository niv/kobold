package es.elv.kobold.glue

import org.nwnx.nwnx2.jvm._

import es.elv.kobold.{G, GCoreClasses}
import es.elv.kobold.host._
import es.elv.kobold.game._
import es.elv.kobold.api._

import com.codahale.logula.Logging
import org.apache.log4j.Level

private [glue] object EventHandler extends SchedulerListener with Logging {
  /** The namespace for game->kobold management calls. */
  val InternalRegexp = """^kobold\.(.+)$""".r

  val TimeLogInterval = 10000

  private var nsThisInterval: Long = 0

  def postFlushQueues(remainingTokens: Int) {}
  def missedToken(objSelf: NWObject, token: String) {}
  def context(objSelf: NWObject) {}

  private def kobold(fun: String, objSelf: IObject) {
    val assocContext = NWScript.getLocalString(Module(), "_" + fun)

    val ret = Context.byUUID(assocContext) match {
      case Some(ctx) =>
        fun match {
          case "attachContext" =>
            Host.attachContext(ctx, Set(objSelf)).size > 0
          case "detachContext" =>
            Host.detachContext(ctx, Set(objSelf)).size > 0
          case "detachContextAll" =>
            Host.detachContextFromAll(ctx)
            true
          case _ => false
        }

      case None =>
        log.warn("game -> " + fun + " failed due to invalid context " +
          assocContext)
        false
    }

    NWScript.setLocalString(Module(), "_" + fun, if (ret) "1" else "0")
  }

  def event(objSelf: NWObject, event: String) = {
    val start = System.nanoTime
    try {
      case class E(e: String, o: NWObject)

      def e[X <: IObject](self: X)(event: String, va: Object*) =
        Host.handleObjectEvent(event, va.toList)(self)

      val ctx: Set[Context[_]] = E(event, objSelf) match {
        case E(InternalRegexp(a), o: IObject) =>
          kobold(a, o)
          Set()

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
          else Set()

        case E("creature_hb", o: ICreature) =>
          e(o)("creature.hb") // Host.onCreatureHB(o) // for task manager!

        case e =>
          log.debug("unhandled: " + e)
          Set()
      }

      Scheduler.flushQueues
    } catch {
      case any =>
        log.error(any, "event = " + event + " on = " + objSelf)

    } finally
      nsThisInterval += System.nanoTime - start
  }

  new java.util.Timer().scheduleAtFixedRate(new java.util.TimerTask {
    def run = if (nsThisInterval != 0) {
      val msIn = nsThisInterval / 1000 / 1000
      val msOut = TimeLogInterval - msIn
      log.debug("Time spent in VM: %dms, in nwserver: %dms => %d%%".format(
        msIn, msOut, msIn / msOut * 100
      ))
      nsThisInterval = 0
    }
  }, 0, TimeLogInterval)

}

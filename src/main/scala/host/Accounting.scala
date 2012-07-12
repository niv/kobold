package es.elv.kobold.host

import com.codahale.logula.Logging

import es.elv.kobold.api._
import es.elv.kobold._

import org.nwnx.nwnx2.jvm.{Scheduler, NWScript, NWObject, NWLocation, NWVector}

trait ContextAccounting {
  this: Context[_] =>

  /** The total time (in ms) this context has taken since creation. */
  var totalRuntime: Long = 0

  /** The time (in ms) this context has taken for this slice. */
  var thisSliceRuntime: Long = 0

  /** The maximum execution time for a single EventHandler. */
  val limitRuntime: Long = 25

  /** The remaining runtime (in ms) for this Context.
    * For now, we're hard-limiting each event to a fixed value.
    * Can be adjusted to a sliding window later on.
    */
  def remainingRuntime = limitRuntime
}

trait Accounting extends Logging {
  this: Host =>

  protected def withAccounting[T](p: => T)
      (implicit ctx: Context[_], obj: IObject): T = {

    val start = System.currentTimeMillis
    try {
      p
    } finally {
      val end = System.currentTimeMillis
      ctx.totalRuntime += (end - start)
      log.debug("%s took %d ms (total %d ms)".format(
        ctx.toString,
        end - start,
        ctx.totalRuntime
      ))
    }
  }
}

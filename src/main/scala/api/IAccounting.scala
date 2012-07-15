package es.elv.kobold.api

import es.elv.kobold._

trait IContextAccounting {
  val quotaEnabled: Boolean = true

  /** This context gains this many milliseconds of execution time quota
    * per realtime second.
    * This is an experimental value and will need tweaking,
    * based on the number of active scripts and other server load.
    * The upper ceiling is about half a second of total time per
    * realtime second, after which nwserver will start exhibiting
    * client-visible latency.
    */
  val msPerSecond = 10

  /** The maximum quota of this context, in microseconds. */
  val maxQuota = 5 * 1000 * 1000

  /** The maximum execution time for a single EventHandler (in us). */
  val limitSingle = 50 * 1000

  /** The total time (in us) this context has eaten since creation. */
  def totalRuntime: Long

  /** The remaining runtime quota(in us) for this Context. */
  def quota: Long

  /** The remaining runtime quota (in us) for this Context, observing
    * all limits for a single invocation. */
  def quotaSingle = if (limitSingle < quota)
    limitSingle else quota
}

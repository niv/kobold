package es.elv.kobold.api

import es.elv.kobold._

private[kobold] trait IContextAccounting {
  /** This context gains this many ms of execution time quota
    * per second.
    * This is an experimental value and will need tweaking,
    * based on the number of active scripts and other server load.
    * The upper ceiling is about half a second of total time per
    * realtime second, after which nwserver will start exhibiting
    * client-visible latency.
    */
  val msPerSecond = 10

  /** The maximum quota of this context, in seconds. */
  val maxQuota = 150

  /** The maximum execution time for a single EventHandler. */
  val limitSingle = 50

  /** The last time this context went active. */
  def lastActiveAt: Long

  /** The total time (in ms) this context has taken since creation. */
  def totalRuntime: Long

  /** The remaining runtime (in ms) for this Context. */
  def quota: Long

  /** The remaining runtime (in ms) for this Context, observing
    * all limits for a single invocation. */
  def quotaSingle = if (limitSingle < quota)
    limitSingle else quota
}

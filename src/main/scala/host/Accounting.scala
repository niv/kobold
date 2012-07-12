package es.elv.kobold.host

import com.codahale.logula.Logging

import es.elv.kobold.api._
import es.elv.kobold._

import org.nwnx.nwnx2.jvm.{Scheduler, NWScript, NWObject, NWLocation, NWVector}

private [host] trait ContextAccounting extends IContextAccounting {
  def lastActiveAt = _lastActiveAt
  private [host] var _lastActiveAt: Long = System.currentTimeMillis

  def totalRuntime = _totalRuntime
  private [host] var _totalRuntime: Long = 0

  def quota = _quota
  private [host] var _quota: Long = 150
}

trait Accounting extends Logging {
  this: Host =>

  protected def withAccounting[T](p: => T)
      (implicit ctx: ContextAccounting, obj: IObject): T = {

    // The number of ms gained since the last time.
    val msGained = (System.currentTimeMillis - ctx._lastActiveAt) / 1000
        ctx.msPerSecond

    ctx._lastActiveAt = System.currentTimeMillis

    ctx._quota += msGained
    if (ctx.quota > ctx.maxQuota) ctx._quota = ctx.maxQuota

    val start = System.currentTimeMillis
    try {
      p
    } finally {
      val diffms = System.currentTimeMillis - start
      ctx._quota -= diffms
      ctx._totalRuntime += diffms

      log.debug("%s quota: %d -%d +%d (total: %d)".format(
        ctx.toString, ctx.quota, diffms, msGained,
        ctx.totalRuntime
      ))
    }
  }
}

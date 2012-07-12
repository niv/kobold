package es.elv.kobold.host

import com.codahale.logula.Logging

import es.elv.kobold.api._
import es.elv.kobold._

import scala.actors.Futures._
import scala.actors.Future

class QuotaExceededException extends RuntimeException

private [host] trait ContextAccounting extends IContextAccounting {
  def lastActiveAt = _lastActiveAt
  private [host] var _lastActiveAt: Long = System.currentTimeMillis

  def totalRuntime = _totalRuntime
  private [host] var _totalRuntime: Long = 0

  def quota = _quota
  private [host] var _quota: Long = 150
}

object Accounting extends Logging {

  def enforceQuota[T](p: => T)
      (implicit ctx: ContextAccounting): T = {

    // The number of ms gained since the last time.
    val msGained = (System.currentTimeMillis - ctx._lastActiveAt) / 1000
        ctx.msPerSecond

    ctx._lastActiveAt = System.currentTimeMillis

    ctx._quota += msGained
    if (ctx.quota > ctx.maxQuota) ctx._quota = ctx.maxQuota

    // No point in hitting the VM if there isn't any quota to run in.
    if (ctx.quota < 1)
      throw new QuotaExceededException

    val start = System.currentTimeMillis
    try {

      awaitAll(ctx.quota, future { p }).
        head.asInstanceOf[Option[T]].
        getOrElse(throw new QuotaExceededException)

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

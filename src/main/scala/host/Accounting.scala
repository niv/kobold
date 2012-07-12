package es.elv.kobold.host

import com.codahale.logula.Logging

import es.elv.kobold.api._
import es.elv.kobold._

import scala.actors.Futures._
import scala.actors.Future

class QuotaExceededException extends RuntimeException

private [host] trait ContextAccounting extends IContextAccounting {
  private [host] var _lastActiveAt: Long = System.nanoTime

  def totalRuntime = _totalRuntime
  private [host] var _totalRuntime: Long = 0

  def quota = _quota
  private [host] var _quota: Long = maxQuota
}

object Accounting extends Logging {

  def enforceQuota[T](p: => T)
      (implicit ctx: ContextAccounting): T =
    withQuota(true, p)(ctx)

  def withQuota[T](enforce: Boolean = false, p: => T)
      (implicit ctx: ContextAccounting): T = {

    val nsLapsed = System.nanoTime - ctx._lastActiveAt
    ctx._lastActiveAt = System.nanoTime
    val usLapsed = nsLapsed / 1000
    val usGained = (usLapsed * ctx.msPerSecond) / 1000

    ctx._quota += usGained
    if (ctx.quota > ctx.maxQuota) ctx._quota = ctx.maxQuota

    // No point in hitting the VM if there isn't any quota to run in.
    if (ctx.quotaEnabled && enforce && ctx.quota <= 100)
      throw new QuotaExceededException

    val start = System.nanoTime
    try {

      if (ctx.quotaEnabled && enforce)
        awaitAll(1 + ctx.quota / 1000, future { p }).
          head.asInstanceOf[Option[T]].
          getOrElse(throw new QuotaExceededException)
      else
        p

    } finally {
      val diffus = (System.nanoTime - start) / 1000
      ctx._quota -= diffus
      ctx._totalRuntime += diffus

      log.debug("%s -%d us +%d us (q: %d ms)".format(
        ctx.toString, diffus, usGained, ctx.quota / 1000
      ))
    }
  }
}

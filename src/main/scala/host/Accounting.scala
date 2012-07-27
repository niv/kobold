package es.elv.kobold.host

import com.codahale.logula.Logging

import es.elv.kobold.api._
import es.elv.kobold._

import scala.actors.Futures._
import scala.actors.Future

class QuotaExceededException(message: String = null)
  extends RuntimeException(message)

private [host] trait ContextAccounting extends IContextAccounting {
  private [host] var _lastActiveAt: Long = System.nanoTime

  def totalRuntime = _totalRuntime
  private [host] var _totalRuntime: Long = 0

  def quota = _quota
  private [host] var _quota: Long = maxQuota

  // Time spent in <category> -> (count, ns)
  type AccountingData = (Long, Long)
  private [host] val timeSpentIn = new
      collection.mutable.HashMap[String, AccountingData]()
      with collection.mutable.SynchronizedMap[String, AccountingData] {
    override def default(k: String) = (0L, 0L)
  }
}

object Accounting extends Logging {

  def trackTime[CTX<:Context[_],T](what: String)(p: => T)
      (implicit ctx: CTX): T = {

    val start = System.nanoTime
    try p finally {
      val (count, ns) = ctx.timeSpentIn(what)
      ctx.timeSpentIn(what) = (count + 1, ns + (System.nanoTime - start))
    }
  }

  def enforceQuota[EH,ENV,CTX<:Context[EH],T](p: => T)
      (implicit ctx: CTX, lang: Language[EH,CTX,ENV]): T =
    withQuota(true, p)(ctx,lang)

  def withQuota[EH,ENV,CTX<:Context[EH],T](enforce: Boolean = false, p: => T)
      (implicit ctx: CTX, lang: Language[EH,CTX,ENV]): T = {

    val nsLapsed = System.nanoTime - ctx._lastActiveAt
    ctx._lastActiveAt = System.nanoTime
    val usLapsed = nsLapsed / 1000
    val usGained = (usLapsed * ctx.msPerSecond) / 1000

    ctx._quota += usGained
    if (ctx.quota > ctx.maxQuota) ctx._quota = ctx.maxQuota

    // No point in hitting the VM if there isn't any quota to run in.
    if (ctx.quotaEnabled && enforce && ctx.quota <= 1000)
      throw new QuotaExceededException("Not enough quota to enter VM")

    val start = System.nanoTime
    try {

      if (ctx.quotaEnabled && enforce) {
        val fu = future { lang.inLanguage(_ => p) }

        awaitAll(1 + ctx.quota / 1000, fu).
          head.asInstanceOf[Option[T]].
          getOrElse(throw new QuotaExceededException("Timeout waiting on return"))

      } else
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

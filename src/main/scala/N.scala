package es.elv.kobold

import host.Host
import host.Context
import host.Accounting

object N extends NWScriptProxyGen {
  protected def wrap[R](method: String)(c: => R): R =
    wrapWithAccounting(method)(c)

  private def wrapWithAccounting[R](method: String)(c: => R) =
    Accounting.trackTime(method)(c)(getContext)


  private def getContext: Context[_] =
    Host.currentContext match {
      case Some(ctx) => ctx
      case None => throw new IllegalStateException("No active context")
    }
}

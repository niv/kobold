package es.elv.kobold.lang.rhino

import org.mozilla.javascript.{Function, Script, Scriptable}
import org.mozilla.javascript.{Context => JSCtx}

import es.elv.kobold.api.IObject
import es.elv.kobold.host.{Host, Context, Language, EventHandler}

class RhinoContext(
  val rhino: RhinoImpl,
  val scope: Scriptable,
  val compiled: Script
) extends Context[Function] {

  override val language = rhino.
    asInstanceOf[Language[Function,Context[Function],JSCtx]]

  def on(eventClass: String, fun: Function) {
    val eht = new EventHandler[Function] {
      def getHandler = fun
    }
    this.registerEvent(eventClass, eht)
  }

  // Threadsafe.
  private def callWithCurrent(a: Function): Any =
    Host.currentObjectSelf match {
      case Some(o: IObject) =>
        rhino.executeEventHandler(o, a, List())(this)
      case _ =>
        throw new IllegalStateException("Not in context")
    }

  def par(a: Function, b: Function): (Any, Any) =
    concurrent.ops.par(
      callWithCurrent(a),
      callWithCurrent(b)
    )
}

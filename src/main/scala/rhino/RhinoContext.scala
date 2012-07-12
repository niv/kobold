package es.elv.kobold.lang.rhino

import org.mozilla.javascript.Function
import org.mozilla.javascript.Script
import org.mozilla.javascript.Scriptable

import es.elv.kobold.host.Context
import es.elv.kobold.host.Language
import es.elv.kobold.host.EventHandler

class RhinoContext(
  val rhino: RhinoImpl,
  val scope: Scriptable,
  val compiled: Script
) extends Context[Function] {

  override val language = rhino.
    asInstanceOf[Language[Function,Context[Function]]]

  def on(eventClass: String, fun: Function) {
    val eht = new EventHandler[Function] {
      def getHandler = fun
    }
    this.registerEvent(eventClass, eht)
  }
}

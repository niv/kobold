package es.elv.kobold.lang.rhino

import org.mozilla.javascript.Function

import es.elv.kobold.script._
import es.elv.kobold.intf._

class ScriptEventRegistryImpl(val ctx: RhinoContext)
    extends IScriptEventRegistry[Function] {

  def set(eventClass: String, fun: Function) {
    val eht = new EventHandler[Function] {
      def getHandler = fun
    }
    ctx.registerEvent(eventClass, eht)
  }
}

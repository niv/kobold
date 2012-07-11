package es.elv.kobold.lang.rhino

import org.mozilla.javascript.Function
import org.mozilla.javascript.Script
import org.mozilla.javascript.Scriptable

import es.elv.kobold.host._

class RhinoContext(
  val rhino: RhinoImpl, //Language[Function,Context[Function]],
  val scope: Scriptable,
  val compiled: Script
) extends Context[Function] {
  
  override val language = rhino.
    asInstanceOf[Language[Function,Context[Function]]]
}

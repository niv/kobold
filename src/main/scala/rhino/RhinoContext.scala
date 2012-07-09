package es.elv.kobold.lang.rhino

import org.mozilla.javascript.Function
import org.mozilla.javascript.Script
import org.mozilla.javascript.Scriptable

import es.elv.kobold.script._

class RhinoContext(val rhino: RhinoImpl, val scope: Scriptable, val compiled: Script)
    extends Context[Function] {
  override val language = rhino.identifier
}

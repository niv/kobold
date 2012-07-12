package es.elv.kobold.host

/** A EventHandler is a Language-specific handler inside a verified Context
  * that can be called without knowing the intrinsics.
  * EH is the actual handler type that Language will handle.
  */
trait EventHandler[EH] {
  def getHandler: EH
}



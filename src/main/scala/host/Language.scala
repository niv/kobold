package es.elv.kobold.host

import es.elv.kobold.api._

trait Language[EH, CTX <: Context[EH]] {
  /** The visible name of this language. Must be unique inside a Host. */
  val name: String

  /** Prepares the given source as a Context. */
  def prepare(source: String): CTX

  /** Execute the given Context.
    * Called by Host, do not call directly.
    */
  def execute(obj: IObject, script: CTX): Any

  /** Execute the given eventhandler on a context.
    * Returns whatever the EH gave back. */
  private [host] def executeEventHandler(obj: IObject,
      eventHandler: EventHandler[EH], va: List[Object])
      (implicit ctx: CTX): Any
}

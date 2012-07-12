package es.elv.kobold.host

import es.elv.kobold.api._
import java.io.InputStream

trait Language[EH, CTX <: Context[EH]] {
  /** The visible name of this language. Must be unique inside a Host. */
  val name: String

  /** Prepares the given source as a Context.
    * This should read, compile, and verify (if possible)
    * the given script. */
  def prepare(source: InputStream): CTX

  /** Execute the given eventhandler on a context.
    * Returns whatever the EH gave back. */
  def executeEventHandler(obj: IObject,
      eventHandler: EventHandler[EH], va: List[Object])
      (implicit ctx: CTX): Any
}

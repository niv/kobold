package es.elv.kobold.host

import es.elv.kobold.api._
import java.io.InputStream

trait Language[EH, CTX <: Context[EH], ENV] {
  /** The visible name of this language. Must be unique inside a Host. */
  val name: String

  /** Prepares the given source as a Context.
    * This should read, compile, and verify (if possible)
    * the given script. */
  def prepare(source: InputStream): CTX

  /** Execute the given eventhandler on a context.
    * Returns whatever the EH gave back. */
  def executeEventHandler(obj: IObject, eventHandler: EH,
    va: List[Any])(implicit ctx: CTX): Any

  /** Runs the given code block within the language environment.
    * Will provide all the neccessary setup and teardown transparently
    * and can be nested arbitarily.
    */
  def inLanguage[T](c: (ENV) => T)(implicit ctx: CTX): T
}

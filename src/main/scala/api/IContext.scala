package es.elv.kobold.api

import java.util.UUID

trait IContext[HANDLER] extends IConcurrency[HANDLER]
    with IContextStore
    with IContextAccounting {
  val uuid: UUID

  /** Register a new event handler for this context. */
  def on(eventClass: String, handler: HANDLER)

  /** Write something to the log of this context. */
  def log(message: String)

  /** Returns the current serverside unix time, with milliseconds.
    * Works just like System.currentTimeMillis() in Java.
    */
  def currentTimeMillis: Long


  /** Returns a list of all objects currently attached to
    * this context.
    */
  def attached: Set[IObject]
}

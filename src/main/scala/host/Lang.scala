package es.elv.kobold.host

import org.nwnx.nwnx2.jvm.NWObject
import es.elv.kobold.api._
import java.util.UUID

/** A EventHandler is a Language-specific handler inside a verified Context
  * that can be called without knowing the intrinsics.
  * EH is the actual handler type that Language will handle.
  */
trait EventHandler[EH] {
  def getHandler: EH
}

/** A Context contains a complete, verified script environment
  * that can handle events, and is bound to a specific Language.
  */
trait Context[EH] {
  val language: Language[EH,Context[EH]]

  val uuid: UUID = UUID.randomUUID

  private var eventHandlers: Map[String, EventHandler[EH]] =
    Map()

  def eventHandlerFor(eventClass: String): Option[EventHandler[EH]] =
    eventHandlers.get(eventClass)

  def registerEvent(eventClass: String, ev: EventHandler[EH]) =
    eventHandlers += ((eventClass, ev))

  /** Executes the given Event on this context. Returns whatever
    * the EH gave back, or None if no handler was ran. */
  def executeEventHandler(host: Host, obj: IObject,
      eventClass: String, va: List[Object]): Option[Any] =
    eventHandlerFor(eventClass) match {
      case Some(eh) =>
        Some(language.executeEventHandler(host, obj, this, eh, va))
      case None => None
    }

  final override def toString: String =
    "%s[%s,%s,%s]".format(
      this.getClass.getName.split("\\.").last,
      uuid.toString,
      eventHandlers.keySet.mkString(";"),
      Host.attachedObjects(this).mkString(";")
    )
}

/**
  */
trait Language[EH, CTX <: Context[EH]] {
  /** The visible name of this language. Must be unique inside a Host. */
  val name: String

  /** Prepares the given source as a Context. */
  def prepare(host: Host, source: String): CTX

  /** Execute the given Context.
    * Called by Host, do not call directly.
    */
  def execute(host: Host, obj: IObject, script: CTX): Any

  /** Execute the given eventhandler on a context.
    * Returns whatever the EH gave back. */
  private [host] def executeEventHandler(host: Host, obj: IObject, script: CTX,
      eventHandler: EventHandler[EH], va: List[Object]): Any
}

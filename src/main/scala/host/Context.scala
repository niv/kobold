package es.elv.kobold.host

import org.nwnx.nwnx2.jvm.NWObject
import es.elv.kobold.api._
import es.elv.kobold.game._
import java.util.UUID

/** A Context contains a complete, verified script environment
  * that can handle events, and is bound to a specific Language.
  */
trait Context[EH] extends IContext[EH]
    with ApacheCommonsPersistency[EH]
    with ContextAccounting {

  val language: Language[EH,Context[EH]]

  val uuid: UUID = UUID.randomUUID

  private var eventHandlers: Map[String, EventHandler[EH]] =
    Map()

  protected def eventHandlerFor(eventClass: String): Option[EventHandler[EH]] =
    eventHandlers.get(eventClass)

  protected def registerEvent(eventClass: String, ev: EventHandler[EH]) =
    eventHandlers += ((eventClass, ev))

  /** Executes the given Event on this context. Returns whatever
    * the EH gave back, or None if no handler was ran. */
  def executeEventHandler(obj: IObject,
      eventClass: String, va: List[Object]): Option[Any] =
    eventHandlerFor(eventClass) match {
      case Some(eh) =>
        Some(language.executeEventHandler(obj, this, eh, va))
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

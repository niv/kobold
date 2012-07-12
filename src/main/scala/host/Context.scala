package es.elv.kobold.host

import org.nwnx.nwnx2.jvm.NWObject
import es.elv.kobold.api._
import es.elv.kobold.game._
import java.util.UUID
import com.codahale.logula.Log

/** A Context contains a complete, verified script environment
  * that can handle events, and is bound to a specific Language.
  */
trait Context[EH] extends IContext[EH]
    with ApacheCommonsPersistency[EH]
    with ContextAccounting {

  val language: Language[EH,Context[EH]]

  val uuid: UUID = UUID.randomUUID

  Context.register(uuid, this)

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

  // TODO: move this to a log store accessible by users/scripts
  private lazy val _log = Log.forClass(this.getClass)
  def log(message: String) {
    _log.info(message)
  }
}

object Context {
  import collection.mutable.{WeakHashMap => WHM}

  private val mapUUID: WHM[String, Context[_]] =
    new WHM()

  private[host] def register(u: UUID, c: Context[_]) {
    mapUUID(u.toString) = c
  }

  def byUUID(c: String) = mapUUID.get(c)
  def byUUID(c: UUID) = mapUUID.get(c.toString)
}

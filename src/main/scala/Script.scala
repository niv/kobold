package es.elv.kobold.script

import org.nwnx.nwnx2.jvm.NWObject
import es.elv.kobold.api._
import java.util.UUID

/**
A EventHandler is a Language-specific handler inside a verified Context
that can be called without knowing the intrinsics.
EH is the actual handler type that Language will handle.
*/
trait EventHandler[EH] {
  def getHandler: EH
}

/**
A Context contains a complete, verified script environment
that can handle events, and is bound to a specific Language. */
trait Context[EH] {
  val language: Language[EH,Context[EH]]

  val uuid: UUID = UUID.randomUUID

  private var eventHandlers: Map[String, EventHandler[EH]] =
    Map()

  def eventHandlerFor(eventClass: String): Option[EventHandler[EH]] =
    eventHandlers.get(eventClass)

  def registerEvent(eventClass: String, ev: EventHandler[EH]) =
    eventHandlers += ((eventClass, ev))

  def executeEventHandler(host: Host, obj: IObject,
      eventClass: String, va: List[Object]) =
    eventHandlerFor(eventClass) match {
      case Some(eh) =>
        language.executeEventHandler(host, obj, this, eh, va)
      case None =>
    }
}

/**
The Host manages registered languages and distributes events
to EventListeners. */
trait Host extends HostEvents {
  // Handle the given event in all registered Contexts.
  def handleObjectEvent(objSelf: IObject, eventClass: String,
    va: List[Object])

  // Attaches the given context to the given host objects.
  def attachContext[EH](ctx: Context[EH], hosts: Set[IObject])
  
  // Detaches the given Context from the given host objects.
  def detachContext[EH](ctx: Context[EH], hosts: Set[IObject])

  // Detaches the given context from all host objects.
  def detachContextFromAll[EH](ctx: Context[EH])

  // Returns a set of all objects that are mapped inside the script host.
  //def mappedObjects: Set[IObject]

  // Returns an IObject descendant for the given oid
  // (either by looking it up or creating a new one)
  //def mappedObjectFor(oid: Int): IObject
}


/* TODO
trait HostListener {
  type Ctx = Context[_]
  def onScriptError(ctx: Ctx, host: IObject, e: Exception)
  def onContextAttached(ctx: Ctx, host: Set[IObject])
  def onContextDetached(ctx: Ctx, host: Set[IObject])
}*/

/**
*/
trait Language[EH, CTX <: Context[EH]] { //  CTX <: Context[EH]] {
  // The visible name of this language. Must be unique inside a Host.
  val name: String

  // Prepares the given source as a Context.
  def prepare(host: Host, source: String): CTX

  // Execute the given Context.
  // Called by Host, do not call directly.
  def execute(host: Host, obj: IObject, script: CTX): Any

  def executeEventHandler(host: Host, obj: IObject, script: CTX,
      eventHandler: EventHandler[EH], va: List[Object]): Any
}

object Language {
  /*private var langMap: Map[String, Language[_,_]] = Map()
  
  def register[E,C <: Context[E]](l: Language[E,C]) =
    langMap += ((l.name, l))

  def unregister(name: String) =
    langMap -= name

  def find[E,C <: Context[E]](name: String): Language[E,C] =
    langMap.get(name).asInstanceOf[Language[E,C]]*/
}

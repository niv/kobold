package es.elv.kobold.script

import org.nwnx.nwnx2.jvm.NWObject
import es.elv.kobold.intf._
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
  val language: String
  val uuid: UUID = UUID.randomUUID

  private var eventHandlers: Map[String, EventHandler[EH]] =
    Map()

  def eventHandlerFor(eventClass: String) =
    eventHandlers(eventClass)
  def registerEvent(eventClass: String, ev: EventHandler[EH]) =
    eventHandlers += ((eventClass, ev))
}

/**
The Host manages registered languages and distributes events
to EventListeners. */
trait Host extends HostEvents {
  type Ctx = Context[_]

  // Handle the given event in all registered Contexts.
  def handleObjectEvent(objSelf: NWObject, eventClass: String,
      va: List[Object])

  // Attaches the given context to the given host objects.
  def attachContext(ctx: Ctx, hosts: Set[NWObject])
  
  // Detaches the given Context from the given host objects.
  def detachContext(ctx: Ctx, hosts: Set[NWObject])

  // Detaches the given context from all host objects.
  def detachContextFromAll(ctx: Ctx)

  // Returns a set of all objects that are mapped inside the script host.
  def mappedObjects: Set[IObject]

  // Returns an IObject descendant for the given oid
  // (either by looking it up or creating a new one)
  def mappedObjectFor(oid: Int): IObject
}

/*
Events the Host implementation needs to handle.
*/
trait HostEvents extends TaskEvents {
  def onCreatureHB(creature: NWObject)
  // def onItemUsed
}

/* TODO
trait HostListener {
  type Ctx = Context[_]
  
  def onManagedCreated(oid: Int, resolved: IObject, associated: Ctx)
  def onManagedTick(oid: Int, resolved: IObject)
  def onManagedDestroyed(oid: Int)

  def onScriptError(ctx: Ctx, host: IObject, e: Exception)

  def onContextAttached(ctx: Ctx, host: Set[IObject])
  def onContextDetached(ctx: Ctx, host: Set[IObject])
}*/

/**
*/
trait Language[EH, CTX <: Context[EH]] {
  // The visible name of this language. Must be unique inside a Host.
  val identifier: String

  // Prepares the given source as a Context.
  def prepare(host: Host, source: String): CTX

  // Execute the given Context.
  // Called by Host, do not call directly.
  def execute(host: Host, obj: IObject, script: CTX): Any

  def executeEventHandler(host: Host, obj: IObject, script: CTX,
      eventHandler: EventHandler[EH], va: List[Object]): Any
}

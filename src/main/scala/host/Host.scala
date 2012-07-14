package es.elv.kobold.host

import com.codahale.logula.Logging

import org.nwnx.nwnx2.jvm.NWObject
import es.elv.kobold.api._
import es.elv.kobold._
import es.elv.kobold.game._

import org.nwnx.nwnx2.jvm.{Scheduler, NWScript, NWObject, NWLocation, NWVector}
import org.nwnx.nwnx2.jvm.constants.ObjectType

/** The Host manages registered languages and distributes events
  * to EventListeners.
  */
trait Host extends HostEvents {
  /** Handle the given event in all registered Contexts.
    * Returns the set of contexts that ran the handler.
    */
  def handleObjectEvent(eventClass: String, va: List[Any])
    (implicit objSelf: IObject): Set[Context[_]]

  /** Attaches the given context to the given host objects.
    * Returns the set of objects that were attached.
    */
  def attachContext[EH](ctx: Context[EH], hosts: Set[IObject]): Set[IObject]

  /** Detaches the given Context from the given host objects.
    * Returns the set of objects that were detached.
    */
  def detachContext[EH](ctx: Context[EH], hosts: Set[IObject]): Set[IObject]

  /** Detaches the given context from all host objects.
    * Returns the set of objects that were detached.
    */
  def detachContextFromAll[EH](ctx: Context[EH]): Set[IObject]

  /** Returns a set of attached objects to the given context. */
  def attachedObjects[EH](ctx: Context[EH]): Set[IObject]

  /** Returns a set of attached contexts to the given object. */
  def attachedContexts(o: IObject): Set[Context[_]]

  /** Returns the current objectSelf, or None if no script is running. */
  def currentObjectSelf: Option[IBase]

  /** Returns the current context, or None if no script is running. */
  def currentContext: Option[Context[_]]

  /** Sends a inter-object message.
    */
  def message(source: IObject, message: Object)
    (implicit target: IObject with ActionQueue)
}

object Host extends Host with Logging {
  private var attachMap: Map[Context[_], Set[IObject]] = Map()

  def attachContext[EH](ctx: Context[EH], hosts: Set[IObject]) = {
    val existing = attachedObjects(ctx)
    if (hosts.size > 0) {
      log.debug("attaching to " + ctx + ": " + hosts)
      attachMap += ((ctx, hosts))
    }
    existing -- hosts
  }

  def detachContext[EH](ctx: Context[EH], hosts: Set[IObject]) = {
    attachMap.get(ctx) match {
      case Some(set) => attachContext(ctx, set -- hosts)
      case None => Set()
    }
  }

  def detachContextFromAll[EH](ctx: Context[EH]) = {
    val existing = attachedObjects(ctx)
    log.debug("detaching " + ctx)
    attachMap -= (ctx)
    existing
  }

  def attachedObjects[EH](ctx: Context[EH]): Set[IObject] =
    attachMap.getOrElse(ctx, Set())

  def attachedContexts(o: IObject): Set[Context[_]] =
    attachedTo(o)

  private def attachedTo(o: IObject): Set[Context[_]] =
    attachMap filter ((s) => s._2 contains o) keySet

  private def convertToAPI(va: List[Any]): List[Any] = {
    va map { e =>
      e match {
        case b: IBase => b
        case o: NWObject => throw new Exception("nooooooo")
        case l: NWLocation =>
          ILocation(G(l.getArea), IVector3(l.getX, l.getY, l.getZ), l.getFacing)
        case v: NWVector =>
          IVector3(v.getX, v.getY, v.getZ)
        case _ => e
      }
    }
  }

  private var _currentObjectSelf: Option[IBase] = None
  private var _currentContext: Option[Context[_]] = None
  def currentObjectSelf = _currentObjectSelf
  def currentContext = _currentContext
  private def withContext[A](c: => A)
      (implicit objSelf: IObject, ctx: Context[_]): A =
    try {
      require(currentObjectSelf.isEmpty)
      _currentObjectSelf = Some(objSelf)
      _currentContext = Some(ctx)
      c
    } finally {
      _currentContext = None
      _currentObjectSelf = None
    }


  def handleObjectEvent(eventClass: String, va: List[Any])
    (implicit objSelf: IObject): Set[Context[_]] = {

    log.debug(eventClass + " -> "  + objSelf + ": " + va)

    def inner(ctx: Context[_]): Boolean = try {
      Accounting.trackTime(eventClass) {
        ctx.executeEventHandler(objSelf,
          eventClass, convertToAPI(va)) match {
            case Some(_) => true
            case None => false
          }
      } (ctx)
    } catch {
      case any =>
        throw any
    }

    attachedTo(objSelf) filter { implicit ctx =>
      withContext {
        try {
          inner(ctx)
        } catch {
          case quota: QuotaExceededException =>
            log.error(quota,
              "Runtime quota exceeded in " + eventClass + " of " + ctx)
            detachContextFromAll(ctx)
            false
        }
      }
    }

  }

  def message(source: IObject, message: Object)
      (implicit target: IObject with ActionQueue) =
    target <= handleObjectEvent("message", List(source, message))

  override def onCreatureHB(c: ICreature) =
    if (attachedTo(c).size > 0) c match {
      case o: NCreature => o.taskManager.tick
      case _ =>
    }

  def onTaskStarted(obj: ICreature with ActionQueue, task: ITask) =
    obj <= handleObjectEvent("task.started", List(task))(obj)
  def onTaskCompleted(obj: ICreature with ActionQueue, task: ITask) =
    obj <= handleObjectEvent("task.completed", List(task))(obj)
  def onTaskCancelled(obj: ICreature with ActionQueue, task: ITask) =
    obj <= handleObjectEvent("task.cancelled", List(task))(obj)
}

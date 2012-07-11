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
  def handleObjectEvent(objSelf: IObject, eventClass: String,
    va: List[Object]): Set[Context[_]]

  /** Attaches the given context to the given host objects. */
  def attachContext[EH](ctx: Context[EH], hosts: Set[IObject])

  /** Detaches the given Context from the given host objects. */
  def detachContext[EH](ctx: Context[EH], hosts: Set[IObject])

  /** Detaches the given context from all host objects. */
  def detachContextFromAll[EH](ctx: Context[EH])

  /** Returns a set of attached objects to the given context. */
  def attachedObjects[EH](ctx: Context[EH]): Set[IObject]

  /** Returns a set of attached contexts to the given object. */
  def attachedContexts(o: IObject): Set[Context[_]]

  /** Returns the current objectSelf, or None if no script is running. */
  def currentObjectSelf: Option[IBase]

  /** Sends a inter-object message.
    * Returns true if the message was delivered successfully.
    */
  def message(source: IObject, target: IObject, message: Object): Boolean
}

object Host extends Host with Logging {
  private var attachMap: Map[Context[_], Set[IObject]] = Map()

  def attachContext[EH](ctx: Context[EH], hosts: Set[IObject]) =
    if (hosts.size > 0) {
      log.debug("attaching to " + ctx + ": " + hosts)
      attachMap += ((ctx, hosts))
    } else
      detachContextFromAll(ctx)


  def detachContext[EH](ctx: Context[EH], hosts: Set[IObject]) =
    attachMap.get(ctx) match {
      case Some(set) => attachContext(ctx, set -- hosts)
      case None =>
    }
  def detachContextFromAll[EH](ctx: Context[EH]) {
    log.debug("detaching " + ctx)
    attachMap -= (ctx)
  }
  
  def attachedObjects[EH](ctx: Context[EH]): Set[IObject] =
    attachMap.getOrElse(ctx, Set())
  
  def attachedContexts(o: IObject): Set[Context[_]] =
    attachedTo(o)

  private def attachedTo(o: IObject): Set[Context[_]] =
    attachMap filter ((s) => s._2 contains o) keySet

  private def convertToAPI(va: List[Object]): List[Object] = {
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
  def currentObjectSelf = _currentObjectSelf
  private def withContext[A](objSelf: IObject)(c: => A): A =
    try {
      require(currentObjectSelf.isEmpty)
      _currentObjectSelf = Some(objSelf)
      c
    } finally {
      _currentObjectSelf = None
    }


  def handleObjectEvent(objSelf: IObject, eventClass: String,
      va: List[Object]): Set[Context[_]] = {

    log.debug(eventClass + " -> "  + objSelf + ": " + va)
    withContext(objSelf) {
      attachedTo(objSelf) filter { ctx =>
        try {
          ctx.executeEventHandler(this, objSelf,
              eventClass, convertToAPI(va)) match {
            case Some(_) => true
            case None => false
          }
        } catch {
          case x =>
            log.error(x, "in " + eventClass + " of " + ctx)
            detachContextFromAll(ctx)
            false
        }
      }
    }
  }

  def message(source: IObject, target: IObject, message: Object) = {
    require(currentObjectSelf.isEmpty)
    handleObjectEvent(target, "message", List(source, message))
    true
  }

  override def onCreatureHB(c: ICreature) =
    if (attachedTo(c).size > 0) c match {
      case o: NCreature => o.taskManager.tick
      case _ =>
    }

  def onTaskStarted(obj: ICreature with ActionQueue, task: ITask) =
    obj <= handleObjectEvent(obj, "task.started", List(task))
  def onTaskCompleted(obj: ICreature with ActionQueue, task: ITask) =
    obj <= handleObjectEvent(obj, "task.completed", List(task))
  def onTaskCancelled(obj: ICreature with ActionQueue, task: ITask) =
    obj <= handleObjectEvent(obj, "task.cancelled", List(task))
}

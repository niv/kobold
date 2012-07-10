package es.elv.kobold.impl.script

import com.codahale.logula.Logging
import es.elv.kobold._
import es.elv.kobold.script._
import es.elv.kobold.intf._
import es.elv.kobold.impl.intf._
import org.nwnx.nwnx2.jvm.{NWScript, NWObject, NWLocation, NWVector}
import org.nwnx.nwnx2.jvm.constants.ObjectType

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

  def handleObjectEvent(objSelf: IObject, eventClass: String,
      va: List[Object]) {
    
    attachedTo(objSelf) foreach { ctx =>
      try {
        ctx.executeEventHandler(this, objSelf, eventClass, convertToAPI(va))
      } catch {
        case x: org.mozilla.javascript.EcmaError =>
          log.error(x, "in " + eventClass + " of " + ctx)
          detachContextFromAll(ctx)

        case any => throw any
      }
    }
  }
  
  //override def onCreatureHB(c: ICreature) =
  //  attachedTo(c) foreach {
  //    _.eventHandlerFor("creature.hb")
  //  }
    //if (true /*isAttachedAnywhereToScript*/)
    //  c.taskManager.tick
      /*_mappedObj.get(c.objectId) match {
        case Some(no) =>
          no.taskManager.tick
        case None =>
      }*/

  def onTaskStarted(task: ITask) {}
  def onTaskCompleted(task: ITask) {}
  def onTaskCancelled(task: ITask) {}
}

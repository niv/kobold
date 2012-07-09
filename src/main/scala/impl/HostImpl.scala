package es.elv.kobold.impl.script

import es.elv.kobold.script._
import es.elv.kobold.intf._
import es.elv.kobold.impl.intf._
import org.nwnx.nwnx2.jvm.{NWScript, NWObject, NWLocation, NWVector}
import org.nwnx.nwnx2.jvm.constants.ObjectType

object Host extends Host {
  def handleObjectEvent(objSelf: NWObject, eventClass: String,
      va: List[Object]) {}

  def attachContext(ctx: Ctx, hosts: Set[NWObject]) {}
  
  def detachContext(ctx: Ctx, hosts: Set[NWObject]) {}

  def detachContextFromAll(ctx: Ctx) {}


  private def convertToAPI(va: List[Object]): List[Object] = {
    va map { e =>
      e match {
        case o: NWObject =>
          getOrCreateManaged(o.getObjectId, None)
        case l: NWLocation => {
          // TODO: refactor into getOrCreateManaged[T]
          val area: IArea = getOrCreateManaged(l.getArea.getObjectId, None) match {
            case aa: IArea => aa
            case _ => throw new ClassCastException
          }
          ILocation(area, IVector3(l.getX, l.getY, l.getZ), l.getFacing)
        }
        case v: NWVector =>
          IVector3(v.getX, v.getY, v.getZ)
        case _ => e
      }
    }
  }

  private var _mappedObj = Map[Int, NObject]()
  // private var _mappedCtx = Map[Int, Boolean]()
  def getOrCreateManaged(oid: Int, ctx: Option[Ctx]): IObject = {
    val applied = NWObject.apply(oid)
    _mappedObj.get(oid) match {
      case Some(mo) => mo
      case None =>
        _mappedObj += ((oid, resolve(applied)))
        _mappedObj(oid)
    }
  }



  def mappedObjects: Set[IObject] = Set()

  def mappedObjectFor(oid: Int): IObject = null

  // resolve the given NWObject to an IObject instance.
  private def resolve[IN <: NWObject](o: IN): NObject = {
    val oid = o.getObjectId
    val otype = NWScript.getObjectType(o)

    otype match {
      case ObjectType.CREATURE => new NCreature(oid)
      case _ => new NObject(oid)
    }
  }

  def onCreatureHB(c: NWObject) {
    _mappedObj.get(c.getObjectId) match {
      case Some(no) =>
        no.taskManager.tick
      case None =>
    }
  }

  def onTaskStarted(task: ITask) {}
  def onTaskCompleted(task: ITask) {}
  def onTaskCancelled(task: ITask) {}
}

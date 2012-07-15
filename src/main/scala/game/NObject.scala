package es.elv.kobold.game

import org.nwnx.nwnx2.jvm.{NWObject, Scheduler}
import es.elv.kobold.api.{IObject, ActionQueue}
import es.elv.kobold.host.Host
import es.elv.kobold.{G, N}

class NoAccessException extends RuntimeException

class NObject(wrapped: NWObject) extends G(wrapped) with IObject
    with ActionQueue {

  protected implicit def n2nw(n: NObject): NWObject =
    n.wrapped
  protected implicit def i2n(o: IObject): NWObject =
    new NWObject(o.objectId)

  def message(target: IObject, message: Object) = target match {
    case vv: IObject with ActionQueue =>
      Host.message(this, message)(vv)
    case _ =>
  }

  def mayAccess = Host.currentObjectSelf match {
    case Some(o) => o.objectId == this.objectId
    case None => false
  }

  protected def checkAccess: Unit = if (!mayAccess)
    throw new NoAccessException

  def name = {
    checkAccess
    N.getName(this, false)
  }

  def destroy {
    checkAccess
    N.destroyObject(this, 1f)
  }
}

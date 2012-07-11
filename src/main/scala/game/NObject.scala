package es.elv.kobold.game

import org.nwnx.nwnx2.jvm.{NWObject, NWScript, Scheduler}
import es.elv.kobold.api.{IBase, IObject, ActionQueue}
import es.elv.kobold.host.Host
import com.codahale.logula.Log
import es.elv.kobold.G

class NoAccessException extends RuntimeException

class NObject(wrapped: NWObject) extends G(wrapped) with IObject with ActionQueue {
  protected implicit def n2nw(n: NObject): NWObject =
    n.wrapped
  protected implicit def i2n(o: IBase): NWObject =
    new NWObject(o.objectId)

  private lazy val _log = Log.forClass(this.getClass)
  
  override def log(message: String) =
    _log.debug(message)

  def message(target: IObject, message: Object) = <= {
    Host.message(this, target, message)
  }

  def mayAccess = Host.currentObjectSelf match {
    case Some(o) => o.objectId == this.objectId
    case None => false
  }

  protected def checkAccess: Unit = if (!mayAccess)
    throw new NoAccessException

  def name = {
    checkAccess
    NWScript.getName(this, false)
  }

  def destroy {
    checkAccess
    NWScript.destroyObject(this, 1f)
  }
}

package es.elv.kobold.game

import org.nwnx.nwnx2.jvm.{NWObject, NWScript, Scheduler}
import es.elv.kobold.api.{IBase, IObject}
import com.codahale.logula.Log
import es.elv.kobold.G

class NObject(wrapped: NWObject) extends G(wrapped) with IObject with ActionQueue {
  protected implicit def n2nw(n: NObject): NWObject =
    n.wrapped
  protected implicit def i2n(o: IBase): NWObject =
    new NWObject(o.objectId)

  private lazy val _log = Log.forClass(this.getClass)
  
  override def log(message: String) =
    _log.debug(message)

  def ipc(target: IObject, message: Object) =
    null // HostImpl.ipc(

  def name = NWScript.getName(this, false)

  def destroy = NWScript.destroyObject(this, 1f)

  def mayAccess = false
}

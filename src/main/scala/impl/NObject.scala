package es.elv.kobold.impl.api

import org.nwnx.nwnx2.jvm.{NWObject, NWScript, Scheduler}
import es.elv.kobold.api.{IBase, IObject}
import com.codahale.logula.Log
import es.elv.kobold.G

// Just some syntax helpers to make it prettier.
trait ActionQueue {
  this: NObject =>
  
  def assign(closure: => Unit) =
    Scheduler.assign(this, new Runnable() {
      def run = closure
    })

  def delay(ms: Long)(closure: => Unit) =
    Scheduler.delay(this, ms, new Runnable() {
      def run = closure
    })
}


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

  lazy val taskManager: TaskManager = new TaskManager

  def name = NWScript.getName(this, false)

  def destroy = NWScript.destroyObject(this, 1f)
}

package es.elv.kobold.impl.intf

import org.nwnx.nwnx2.jvm.NWObject
import org.nwnx.nwnx2.jvm.NWScript
import es.elv.kobold.intf.IObject

class NObject(private val oid: Int) extends IObject {
  implicit def n2nw(nw: NObject): NWObject =
    nw.wrapped
  protected lazy val wrapped = NWObject.apply(oid)


  def ipc(target: IObject, message: Object) =
    null // HostImpl.ipc(

  lazy val taskManager: TaskManager = new TaskManager

  def objectId = oid

  def isValid = NWScript.getIsObjectValid(this)

  def name = NWScript.getName(this, false)

  def destroy = NWScript.destroyObject(this, 1f)
}

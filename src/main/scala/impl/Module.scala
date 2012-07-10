package es.elv.kobold.impl.intf

import org.nwnx.nwnx2.jvm.NWObject
import org.nwnx.nwnx2.jvm.NWScript
import es.elv.kobold.intf._

private [intf] class Module extends NObject(new NWObject(0)) with IModule {
}

object Module {
  private val instance = new Module
  def apply() = instance
}

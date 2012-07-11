package es.elv.kobold.game

import org.nwnx.nwnx2.jvm.NWObject
import org.nwnx.nwnx2.jvm.NWScript
import es.elv.kobold.api._

class Module extends NObject(new NWObject(0)) with IModule {
}

object Module {
  private val instance = new Module
  def apply() = instance
}

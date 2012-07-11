package es.elv.kobold.script

import es.elv.kobold.api._
import org.nwnx.nwnx2.jvm.NWObject

/** Events the Host implementation needs to handle.
  * Regular ingame events on actors are sent via
  * Host.onObjectEvent(); this is for API logic
  * and modules.
  */
trait HostEvents extends TaskEvents {
  /*
  def onModuleLoad(module: IModule) {}
  def onModuleHB(module: IModule) {}
  def onCreatureSpawn(creature: ICreature) {}
  def onCreatureHB(creature: ICreature) {}
  */
}


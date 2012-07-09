package es.elv.kobold.impl.intf

import org.nwnx.nwnx2.jvm.NWObject
import org.nwnx.nwnx2.jvm.NWScript
import es.elv.kobold.intf._

class NCreature(oid: Int) extends NObject(oid) with ICreature {
  lazy val isPlayer = NWScript.getIsPC(this)
}

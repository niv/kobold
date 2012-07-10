package es.elv.kobold.test

import org.scalacheck._
import org.scalacheck.Prop._
import org.nwnx.nwnx2.jvm._

object NWScriptSpecification extends Properties("NWScript") {
  implicit def arbNWObj: Arbitrary[NWObject] =
    Arbitrary {
      new NWObject(new scala.util.Random().nextInt(0x7ffffffe))
    }

  /*property("getName") = forAll { a: NWObject =>
    NWScript.getName(a) == "name-" + a.getObjectId
  }*/
}

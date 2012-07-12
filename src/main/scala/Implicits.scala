package es.elv.kobold

import org.nwnx.nwnx2.jvm._
import api._

/** Some implicits that help writing conscise API methods.
  * Import sparingly - errors are hard to debug.
  */
object Implicits {
  //implicit def eff2nweff(n: Effect): NWEffect = n.toNWEffect
  //implicit def iprpj2nwiprp(n: ItemProperty): NWItemProperty = n.toNWItemProperty

  implicit def go2nwobj(n: G): NWObject =
    n.wrapped

  implicit def loc2nwloc(l: ILocation): NWLocation =
    new NWLocation(
      new NWObject(l.area.objectId),
      l.position.x, l.position.y, l.position.z,
      l.facing
    )

  implicit def nwloc2loc(l: NWLocation): ILocation =
    ILocation(
      G(l.getArea),
      IVector3(l.getX, l.getY, l.getZ),
      l.getFacing
    )

  implicit def nwloc2loc(l: NWVector): IVector3 =
    IVector3(l.getX, l.getY, l.getZ)

  implicit def vec2nwvec(l: IVector3): NWVector =
    new NWVector(l.x, l.y, l.z)

  implicit def ivec2vec(v: (Float, Float, Float)): IVector3 =
    IVector3(v._1, v._2, v._3)
}

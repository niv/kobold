package es.elv.kobold.api

/** A two-axis vector. */
trait IVector2 {
  val x: Float
  val y: Float
}

/** A three-axis vector, representing the NWScript Vector. */
trait IVector3 {
  val x: Float
  val y: Float
  val z: Float
}

object IVector2 {
  def apply(_x: Float, _y: Float) =
    new IVector2 {
      val x = _x
      val y = _y

      override def toString = "IVector2(%f,%f)".format(x,y)
    }
}

object IVector3 {
  val ORIGIN = IVector3(0, 0, 0)
  
  def apply(_x: Float, _y: Float, _z: Float) =
    new IVector3 {
      val x = _x
      val y = _y
      val z = _z
      
      override def toString = "IVector3(%f,%f,%f)".format(x,y,z)
    }
}

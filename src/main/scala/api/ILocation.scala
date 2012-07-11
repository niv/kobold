package es.elv.kobold.api

trait ILocation {
  val area: IArea
  val position: IVector3
  val facing: Float
}

object ILocation {
  def apply(_area: IArea, _position: IVector3, _facing: Float) =
    new ILocation {
      val area = _area
      val position = _position
      val facing = _facing

      override def toString = "Location(%s,%s,%f)".format(area,position,facing)
    }
}

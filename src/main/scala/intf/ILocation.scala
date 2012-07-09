package es.elv.kobold.intf

case class ILocation(
  val area: IArea,
  val position: IVector3,
  val facing: Float
)

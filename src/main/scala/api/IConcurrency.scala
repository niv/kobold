package es.elv.kobold.api

trait IConcurrency[HANDLER] {
  /** Run two handlers in parallel. Note that this will actually
    * run in separate threads. Will return when BOTH handlers have
    * finished. Will count only once against quota.
    */
  def par(a: HANDLER, b: HANDLER): (Any, Any)
}

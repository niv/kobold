package es.elv.kobold.api

import org.nwnx.nwnx2.jvm.{NWObject, NWScript, Scheduler}

/** Any object that can be created, located, destroyed and modified
  * inherits this trait.
  *
  * Also includes some syntax helpers to make objects with
  * Action Queues prettier.
  */
trait IActionable {
  this: IObject =>

  protected [kobold] def <=(closure: => Unit): Unit =
    Scheduler.assign(new NWObject(this.objectId), new Runnable {
      def run { closure }
    })

  protected [kobold] def <+(ms: Long)(closure: => Unit): Unit =
    Scheduler.delay(new NWObject(this.objectId), ms, new Runnable {
      def run { closure }
    })

  /** Destroy this object, unmap it from the Script Host, and
    * remove all running script handlers. This is non-reversible.
    */
  def destroy

  /** Gets the name of this Object. */
  def name: String

  /**
   * Will return the distance to other, in fraction of meters.
   * Will return for zero if the given object is not in perception
   * range.
   */
  //float distanceTo(IObject other);
}

package es.elv.kobold.api

import org.nwnx.nwnx2.jvm.{NWObject, NWScript, Scheduler}

/** Just some syntax helpers to make objects with
  * Action Queues prettier.
  */
trait ActionQueue {
  this: IObject =>
  
  private [kobold] def <=(closure: => Unit): Unit =
    Scheduler.assign(new NWObject(this.objectId), new Runnable {
      def run { closure }
    })

  private [kobold] def <+(ms: Long)(closure: => Unit): Unit =
    Scheduler.delay(new NWObject(this.objectId), ms, new Runnable {
      def run { closure }
    })
}

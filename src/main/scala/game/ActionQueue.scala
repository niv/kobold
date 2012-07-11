package es.elv.kobold.game

import org.nwnx.nwnx2.jvm.{NWObject, NWScript, Scheduler}

/** Just some syntax helpers to make objects with
  * Action Queues prettier.
  */
trait ActionQueue {
  this: NObject =>
  
  def <=(closure: => Unit): Unit =
    Scheduler.assign(this, new Runnable {
      def run { closure }
    })

  def <+(ms: Long)(closure: => Unit): Unit =
    Scheduler.delay(this, ms, new Runnable {
      def run { closure }
    })
}

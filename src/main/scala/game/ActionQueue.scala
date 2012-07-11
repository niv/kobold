package es.elv.kobold.game

import org.nwnx.nwnx2.jvm.{NWObject, NWScript, Scheduler}

/** Just some syntax helpers to make objects with
  * Action Queues prettier.
  */
trait ActionQueue {
  this: NObject =>
  
  def assign(closure: => Unit) =
    Scheduler.assign(this, new Runnable() {
      def run = closure
    })

  def delay(ms: Long)(closure: => Unit) =
    Scheduler.delay(this, ms, new Runnable() {
      def run = closure
    })
}

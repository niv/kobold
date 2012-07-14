package es.elv.kobold.game

import es.elv.kobold.N
import es.elv.kobold.api._
import es.elv.kobold.host._

import org.nwnx.nwnx2.jvm.{NWObject,Scheduler}

import com.codahale.logula.Logging

abstract class TaskableAction(
  val taskAction: Int, protected val host: NWObject
) extends BaseTask with Logging {

  override final def tick {
      if (!N.getIsObjectValid(host)) {
        log.debug("host invalid, stopping ticking")
        cancel
      } else {
        val cur = N.getCurrentAction(host)
        log.debug("tick: " + cur)
        if (cur != taskAction && isActionPossible) {
          log.debug("current action != taskAction, resetting")
          doAction
        }
      }
  }

  override final def cancel {
    super.cancel
    stopDoingAction
  }

  // You can override this if neccessary.
  protected def stopDoingAction {
    N.clearAllActions(false)
    log.debug("stopAction")
  }

  protected def isActionPossible: Boolean
  protected def doAction

  // Start the first action immediately.
  doAction
}

class FollowTask(
  follower: NWObject,
  private val toFollow: NWObject,
  private val followDistance: Float = 5
) extends TaskableAction(35 /*ACTION_FOLLOW*/, follower) {

  log.debug(host + " following " + toFollow)

  def isActionPossible = !N.getIsInCombat(host)

  def doAction {
    log.debug("doAction")
    Scheduler.assign(toFollow, new Runnable {
      def run {
        N.clearAllActions(false)
        N.actionForceFollowObject(toFollow, followDistance)
      }
    })
  }
}

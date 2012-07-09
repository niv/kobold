package es.elv.kobold.glue

import org.nwnx.nwnx2.jvm._

import es.elv.kobold.impl.script.Host

// Dummy object handler that just passes on NWObject. This should
// be later refactored to do I*->N* conversation instead of our
// custom-rolled impl in Host.resolve().
object ObjectHandler extends NWObject.ObjectHandler {
  def handleObjectClass(obj: NWObject, valid: Boolean, objType: Int,
        resRef: String, tag: String) =
      obj
}

object EventHandler extends SchedulerListener {
  def postFlushQueues(remainingTokens: Int) {}
  def missedToken(objSelf: NWObject, token: String) {}
  def context(objSelf: NWObject) {}
  
  def event(objSelf: NWObject, event: String) {
    event match {
      case "creature_hb" => Host.onCreatureHB(objSelf)
      case _ =>
    }
  }

}

object Init {
  def setup {}
  
  def init {
    Scheduler addSchedulerListener EventHandler
    NWObject registerObjectHandler ObjectHandler
    //NWObject registerEffectHandler EffectHandler
    //NWObject registerItemPropertyHandler IPropHandler
    
    // TODO: read all scripts & start up Host
  }
  
  def shutdown {
    // ?
  }
}

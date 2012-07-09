package es.elv.kobold.intf

trait IScriptEventRegistry[HANDLER] {
  def set(eventClass: String, handler: HANDLER)
// void log(Object... message);
}

package es.elv.kobold.api

trait IScriptEventRegistry[HANDLER] {
  def set(eventClass: String, handler: HANDLER)
// void log(Object... message);
}

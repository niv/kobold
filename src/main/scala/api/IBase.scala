package es.elv.kobold.api

trait IBase {
  /** Returns the internal object id. Useful for debugging.
    * Will change between server restarts.
    */
  def objectId: Int

  /** Returns true if this Object is valid and still mapped
    * for this ScriptHost. Accessing any object that isn't mapped
    * & valid will result in a NoAccessException being thrown.
    */
  def isValid: Boolean

  /** Will return true if the currently-running script has
    * full access to this object. Accessing any object that
    * the running script has no access to will result in a
    * NoAccessException being thrown, and as such checking
    * with this method is advisable.
    */
  def mayAccess: Boolean
}

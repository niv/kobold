package es.elv.kobold.api

/** The base game object, from which others inherit functionality
  * common to all objects.
  */
trait IObject {
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

  /**
   * Send a custom message to this object.
   * Any context attached to this object will receive
   * the "message" event.
   *
   * There are no guarantees that the message is delivered (or
   * acted upon), since the event runs entirely asynchronously
   * and may be delayed by the server as required.
   */
  def message(message: Object)
}

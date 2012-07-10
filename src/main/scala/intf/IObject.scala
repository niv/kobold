package es.elv.kobold.intf

trait IBase {
  /**
	 * Returns the internal object id. Useful for debugging.
	 * Will change between server restarts.
	 */
	def objectId: Int
	
	/**
	 * Returns true if this Object is valid and still mapped
	 * for this ScriptHost. Accessing any object that isn't mapped
	 * & valid will result in a NoAccessException being thrown.
	 */
  def isValid: Boolean
	

  /**
	 * Write something to the script log.
	 */
  def log(message: String) {}
}

/**
 * The base game object, from which others inherit functionality
 * common to all objects.
 */
trait IObject extends IBase /*extends IPersistency*/ {
	/**
	 * Send a custom IPC message to another object.
	 * The target object will receive the "script.ipc" event.
	 */
	def ipc(target: IObject, message: Object)


	/**
	 * Destroy this object, unmap it from the Script Host, and
	 * remove all running script handlers. This is non-reversible.
	 */
	def destroy
	
	/**
	 * Gets the name of this Object.
	 */
  def name: String
	
	/**
	 * Sets the visible name of this object. 
	 */
	// void setName(String name);
	

	/**
	 * Will return true if the currently-running script has
	 * full access to this object. Accessing any object that
	 * the running script has no access to will result in a
	 * NoAccessException being thrown.
	 */
	//boolean mayAccess();
	
	/**
	 * Will return the distance to other, in fraction of meters.
	 * Will return for zero if the given object is not in perception
	 * range.
	 */
	//float distanceTo(IObject other);
}

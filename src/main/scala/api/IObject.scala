package es.elv.kobold.api

/** The base game object, from which others inherit functionality
  * common to all objects.
  */
trait IObject extends IBase /*extends IPersistency*/ {
	/**
	 * Send a custom message to another object.
	 * The target object will receive the "message" event.
   * There are no guarantees that the message is delivered (or
   * acted upon), since the event runs entirely asynchronously
   * and may be delayed by the server as required.
	 */
	def message(target: IObject, message: Object)


	/** Destroy this object, unmap it from the Script Host, and
	  * remove all running script handlers. This is non-reversible.
	  */
	def destroy
	
	/** Gets the name of this Object. */
  def name: String
	
	/**
	 * Sets the visible name of this object. 
	 */
	// void setName(String name);
	


	/**
	 * Will return the distance to other, in fraction of meters.
	 * Will return for zero if the given object is not in perception
	 * range.
	 */
	//float distanceTo(IObject other);
}

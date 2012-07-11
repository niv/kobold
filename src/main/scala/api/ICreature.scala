package es.elv.kobold.api

/**
A Creature in the game. This includes NPCs, all monsters, and
player characters, but NOT dungeon masters.
*/
trait ICreature extends IObject {
  /** Forgets all tasks, stops all timers, and clears
    * any associated local state.
    */
  //def clear
  
  /** Returns the number of current task list of this Creature.
    */
  //def taskCount: Int
  
  /** Return the task list of this Creature. May be empty.
    * The active task is at the head of the list.
    */
  //def taskList: List[ITask]
  
  /** Return true if this Creature is a player character.
    */
  def isPlayer: Boolean
  
  /** Makes this creature say the given text immediately.
    */
  def say(message: String)
  
  /** Makes this creature whisper the given text immediately.
    */
  def whisper(message: String)
  
  /** Will return true if this Creature can see the Creature other.
    */
  def sees(other: ICreature): Boolean
  
  /** Returns all perceived Creatures nearby (heard, seen).
    * Will not return placeables, items, objects.
    * 
    * Distance: the distance in meters. Limited to 0-30.
    */
  def getPerceivedCreatures(distance: Float): Array[ICreature]
  
  /** Returns the location of this Creature. 
    */
  def location: ILocation
  
  /* Movement */
  
  /** Makes this Creature follow other, with the given distance.
    * 
    * Will enqueue as a task.
    */
  //def taskFollow(other: ICreature, followDistance: Float): ITask
  
  /** Task this creature to walk to the given waypoint.
    */
  // def taskWalk(IWaypoint whereTo)
  
  /** Task this creature to run to the given location.
    */
  // def taskRun(IWaypoint whereTo)
  
  /* Items & Inventory */
  
  /** Makes this Creature equip the given item. The item must be in this
    * Creatures inventory and be script-accessible.
    */
  //ITask taskEquipItem(IItem item)
  
  /** Makes this Creature unequip the given item. The item must be in
    * this Creatures inventory, be script-accessible, and currently equipped.
    */
  //ITask taskUnequipItem(IItem item)
  
  /** Returns all Items nearby. Will only return items this
    * Creature can see.
    * 
    * Distance: the distance in meters. Limited from 1 to 30, inclusive.
    */
  //IItem[] getVisibleItems(float distance)
  
  /** Makes this Creature pick up the given item.
    * Will only work for items currently in visible range (i.e. via getVisibleItems)
    */
  //ITask taskPickupItem(IItem item)
  
  /** Returns all (script-accessible) items currently in this Creatures inventory.
    */
  //IItem[] getInventoryItems
}

package es.elv.kobold.api

trait ISystem {
  /** Returns the current serverside unix time, with milliseconds.
    * Works just like System.currentTimeMillis() in Java.
    */
  def currentTimeMillis: Long
}

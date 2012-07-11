package es.elv.kobold.api

// An area in the game
trait IArea extends IObject {
  /**
   * Returns the weather in the given area.
   * One of:
   *  WEATHER_CLEAR, WEATHER_RAIN, WEATHER_SNOW, WEATHER_INVALID
   *  (tbd)  
   */
  //def weather: Int
}

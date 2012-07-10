package es.elv.kobold

object Types {
  type ObjectType = Int
}

/*
int    OBJECT_TYPE_CREATURE         = 1;
int    OBJECT_TYPE_ITEM             = 2;
int    OBJECT_TYPE_TRIGGER          = 4;
int    OBJECT_TYPE_DOOR             = 8;
int    OBJECT_TYPE_AREA_OF_EFFECT   = 16;
int    OBJECT_TYPE_WAYPOINT         = 32;
int    OBJECT_TYPE_PLACEABLE        = 64;
int    OBJECT_TYPE_STORE            = 128;
int    OBJECT_TYPE_ENCOUNTER        = 256;
int    OBJECT_TYPE_ALL              = 32767;
int    OBJECT_TYPE_INVALID          = 32767;
*/
object ObjectType {
  val Creature = 1
  val Item = 2
  val Trrigger = 4
  val Door = 8
  val AoE = 16
  val Waypoint = 32
  val Placeable = 64
  val Store = 128
  val Encounter = 256

  val All = 32767
  val InvalidObject = All
}

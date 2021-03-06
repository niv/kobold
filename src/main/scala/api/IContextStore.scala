package es.elv.kobold.api

import java.util.UUID

class StorageException extends RuntimeException

trait IContextStore {
  /** Store the given variable persistently between
    * server restarts, keyed to this script context.
    * You can store any valid javascript object, including
    * arrays, but not runtime-specifics (like game object references).
    *
    * The running script may be subject to storage quota,
    * and over-quota will result in a StorageException being thrown.
    *
    * If persistent storage is not available, a StorageException will
    * be thrown.
    */
  def set(key: String, value: Any)

  /** Retrieve the given variable from persistent storage.
    * Will return null when not found.
    *
    * If persistent storage is not available, a StorageException will
    * be thrown.
    */
  def get(key: String): Any

  /** Deletes the given variable, returning it's value.
    */
  def delete(key: String): Any

  /** Clears out the storage and deletes all content. */
  def clear
}

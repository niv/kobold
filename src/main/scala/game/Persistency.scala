package es.elv.kobold.game

import es.elv.kobold.host._
import es.elv.kobold.api.IContext
import es.elv.kobold.api.IContextStore

import com.codahale.logula.Logging
import org.apache.commons.configuration._

import concurrent.ops.spawn

trait Persistency[H] extends Logging {
  this: IContext[H] =>

  protected def getStore: IContextStore

  protected def withStore[T](block: (IContextStore) => T): T =
    block(getStore)

  def set(key: String, value: Any) =
    withStore { _.set(key, value) }

  def get(key: String): Any =
    withStore { _.get(key) }

  def delete(key: String): Any =
    withStore { _.delete(key) }

  def clear = withStore { _.clear }
}

trait ApacheCommonsPersistency[H] extends Persistency[H] {
  this: IContext[H] =>

  private lazy val config = {
    val configurationFile =
      new java.io.File(uuid.toString + ".store")
    val c = if (configurationFile.exists)
      new PropertiesConfiguration(configurationFile)
    else
      new PropertiesConfiguration
    c.setAutoSave(true)
    c.setFileName(configurationFile.getName())
    c
  }

  protected class CommonsStore(cfg: PropertiesConfiguration)
      extends IContextStore {
    def set(key: String, value: Any) =
      spawn { cfg.setProperty(key, value) }
    def get(key: String): Any =
      cfg.getString(key)
    def delete(key: String): Any = {
      val i = get(key)
      spawn { cfg.clearProperty(key) }
      i
    }
    def clear = spawn { cfg.clear }
  }

  private lazy val store = new CommonsStore(config)

  protected def getStore = store
}

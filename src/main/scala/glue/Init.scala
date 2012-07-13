package es.elv.kobold.glue

import org.nwnx.nwnx2.jvm._

import es.elv.kobold.{G, GCoreClasses}
import es.elv.kobold.host._
import es.elv.kobold.game._
import es.elv.kobold.api._
import es.elv.kobold.lang.rhino._

import com.codahale.logula.Logging
import org.apache.log4j.Level

import org.apache.commons.configuration._
import org.apache.commons.configuration.reloading._
import scala.collection.JavaConverters._
import java.io.File

private [glue] object Init extends Logging {
  if (System.getSecurityManager == null &&
      System.getProperty("kobold.pleasePwnMe") == null)
    throw new Error("It is STRONGLY recommended to use a security manager! " +
      "If you are SURE you want to run without one, supply -Dkobold.pleasePwnMe")

  Logging.configure { log =>
    log.registerWithJMX = false
    log.level = Level.DEBUG
    log.console.enabled = true
    log.console.threshold = Level.DEBUG
    log.loggers("es.elv.kobold.G") = Level.WARN
  }

  if (null == System.getSecurityManager)
    log.warn("RUNNING WITHOUT SECURITYMANAGER. This is a BAD IDEA, as it will " +
      "potentially open you up to malicious code messing with your system.")

  val home = System.getProperty("kobold.home")

  private val rhino = new RhinoImpl

  private val attachMap = new PropertiesConfiguration(home + "/attach.properties")
  attachMap.setReloadingStrategy(new FileChangedReloadingStrategy())
  attachMap.setAutoSave(true)

  def setup {
    /*log.info("Loading and preparing scripts")
    val scriptDir = config.getString("scriptDir")
    val scripts: Array[File] = for (f <- new File(scriptDir).listFiles if
        """\.js$""".r.findFirstIn(f.getName).isDefined
      ) yield f

    scripts foreach { f  =>
      log.info("Loading " + f.getName)
      val src = io.Source.fromFile(f).mkString
      val script =  rhino.prepare(Host, src)
      rhino.execute(Host, null, script)
    }*/

    log.info("Finding static attachments")
    val n = for (str <- attachMap.getStringArray("byOID"))
        yield (str.split(";").toList)

    n foreach { el: List[String] => el match {
      case scr :: tail =>
        log.info("Script " + scr + " attachedTo " + tail)
        val oids = tail map (_.toInt)
        val script =  rhino.prepare(new java.io.FileInputStream(scr))
        //rhino.execute(null, script)
        Host attachContext (script, oids.map { oid =>
          new NCreature(new NWObject(oid)) }.toSet)

      case _ => throw new Exception("invalid byOID config")
    }
  } }

  def init {
    GCoreClasses.registerAll

    Scheduler addSchedulerListener EventHandler

    // The default object handler just passes all requests on to
    // G[T](), which will spit out NWhatevers, all implemenations of
    // IBase -> IObject -> IWhatever
    NWObject registerObjectHandler new NWObject.ObjectHandler {
      def handleObjectClass(obj: NWObject, valid: Boolean, objType: Int,
            resRef: String, tag: String): NWObject =
        G(obj)
    }
  }

  def shutdown {}
}

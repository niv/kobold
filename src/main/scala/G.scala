package es.elv.kobold

import org.nwnx.nwnx2.jvm.{NWObject, NWScript}
import com.codahale.logula.Logging

import Implicits._
import Types._

private[kobold] abstract class G(protected [kobold] val wrapped: NWObject)
    extends NWObject(wrapped.getObjectId) {

	val objCreatedAt = System.currentTimeMillis
	def objAge = System.currentTimeMillis - objCreatedAt
  
  def isValid = NWScript.getIsObjectValid(this)

  lazy val objectId = this.getObjectId

  override lazy val objectType = NWScript.getObjectType(this)

  final override def toString: String =
    "%s[%x]".format(this.getClass.getName.split("\\.").last,objectId)
}

object G extends Logging {
	private var objectClasses: List[(NWObject, Boolean, ObjectType,
      String, String) => Option[G]] = List()

	/**
		Register a factory that produces instances of G.
		You need to be as specific as possible in your matching, or you will break something.
		Return Some(your G) if you want to handle this NWObject with your class.
		Return None if the given NWObject does not match your custom class.
		The parameters passed in are: NWObject, getIsObjectValid, ObjectType, ResRef, Tag
	*/
	def registerCustomClass(factory: (NWObject, Boolean, ObjectType, String, String) => Option[G]) =
		objectClasses = factory :: objectClasses

	private[kobold] def registerObjectClass(factory: (NWObject, Boolean, ObjectType, String, String) => Option[G]) =
		objectClasses = objectClasses ::: List(factory)

	def apply[K <: G](n: Int): K =
		apply[K](new NWObject(n))

	def apply[K <: G](o: NWObject): K = {
    val valid = NWScript.getIsObjectValid(o)
    val objectType = NWScript.getObjectType(o)
    val resRef = NWScript.getResRef(o)
    val tag = NWScript.getTag(o)

    def selectClass(facList: List[ (NWObject, Boolean, ObjectType, String, String) => Option[G] ]): Option[G] = {
      for (fact <- facList)
        try {
          fact(o, valid, objectType, resRef, tag) match {
            case Some(g) => return Some(g)
            case None =>
          }
        } catch {
          case p: IllegalArgumentException => {
            log.error("while trying to produce: %08x=%s valid=%s ref=%s tag=%s".format(
              o.getObjectId, valid.toString, objectType, resRef, tag), p)
            throw p
          }
        }
      return None
    }

    val kk: G = selectClass(objectClasses) match {
      case Some(g) => g
      case None => throw new Exception(
        "Cannot produce for %08x=%s valid=%s ref=%s tag=%s".format(
          o.getObjectId, valid.toString, objectType, resRef, tag))
    }

    log.debug("%08x=%s valid=%s ref=%s tag=%s -> %s".format(
      o.getObjectId, valid.toString, objectType.toString, resRef, tag, kk))

    kk.asInstanceOf[K]
	}

	/**
		Returns the first object with the given tag.
	*/
	def byTag(tag: String): G = byTag(tag, 0)

	/**
		Returns the nth object with the given tag.
	*/
	def byTag(tag: String, index: Int): G =
		G(NWScript.getObjectByTag(tag, index))

	/**
		Returns a list of all objects with the given tag.
	*/
	//def allByTag(tag: String): List[G] =
	//	NWScript.allByTag(tag).map(G(_)).toList

	/**
		Returns all objects with the given tag of the given klass.
	*/
	/*def allByTag[K <: G](tag: String, klass: Class[K]): List[K] =
		NWScript.allByTag(tag).map(x => G[G](x)).
			filter(x => klass.isAssignableFrom(x.getClass)).
			map(x => x.asInstanceOf[K]).toList
*/
}

package es.elv.kobold.lang.rhino

import org.mozilla.javascript._

/** This WF adds support for converting various scala types:
  *
  *  - List[Any]
  *  - Product (Tuples, for example)
  */
class ScalaSecureWrapFactory extends DefaultSecureWrapFactory {
	override def wrap(cx: Context, scope: Scriptable, obj: Object,
      staticType: Class[_]) =

    obj match {
      case ll: List[_] =>
		    super.wrap(cx, scope, ll.asInstanceOf[List[Any]].toArray,
          classOf[Array[_]])

      case ll: Set[_] =>
		    super.wrap(cx, scope, ll.asInstanceOf[Set[Any]].toArray,
          classOf[Array[_]])

      case tu: Product =>
		    super.wrap(cx, scope, tu.asInstanceOf[Product].productIterator.toArray,
          classOf[Array[_]])

      case _ =>
		    super.wrap(cx, scope, obj, staticType)
    }

}

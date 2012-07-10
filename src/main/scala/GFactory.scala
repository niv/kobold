package es.elv.kobold

import org.nwnx.nwnx2.jvm.{NWObject, NWScript}
import intf._

import Implicits._
import Types._

abstract class GSelector[K <: G]

abstract class GFactory[K <: G](val objectType: ObjectType) extends GSelector[K] {
	protected def create(resref: String, where: ILocation, useAnimation: Boolean, newTag: String) = {
		val r = G[G](NWScript.createObject(objectType, resref, where, useAnimation, newTag))
		if (r.isValid)
			postCreate(r.asInstanceOf[K])
		r
	}

	def apply(resref: String, where: ILocation): G =
		apply(resref, where, false, "")
	def apply(resref: String, where: ILocation, useAnimation: Boolean): G =
		apply(resref, where, useAnimation, "")
	def apply(resref: String, where: ILocation, useAnimation: Boolean, newTag: String): G =
		create(resref, where, useAnimation, newTag)
	
  /** Override this for post-creation hook. Note that this will not fire if instanciation fails. */
	protected def postCreate(o: K) {}
}

abstract class GRefFactory[K <: G](objectType: ObjectType, val resref: String)
		extends GFactory[K](objectType) {

	def apply(where: ILocation): G =
		apply(where, false, "")
	def apply(where: ILocation, useAnimation: Boolean): G =
		apply(where, useAnimation, "")
	def apply(where: ILocation, useAnimation: Boolean, newTag: String): G =
		create(resref, where, useAnimation, newTag)
}

abstract class GRefTagFactory[K <: G](objectType: ObjectType, resref: String, val tag: String)
		extends GRefFactory[K](objectType, resref) {

	override def create(resref: String, where: ILocation, useAnimation: Boolean, newTag: String) =
		super.create(resref, where, useAnimation, tag)
}

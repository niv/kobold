package es.elv.kobold.lang.rhino

import org.mozilla.javascript.{Context => JSCtx}
import org.mozilla.javascript.DefaultSecureWrapFactory
import org.mozilla.javascript.Function
import org.mozilla.javascript.Script
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.SecureClassShutter
import org.mozilla.javascript.SecureScriptRuntime
import org.mozilla.javascript.ScriptTimeoutError
import org.mozilla.javascript.TimingContextFactory

import es.elv.kobold.api._
import es.elv.kobold.host.Language
import es.elv.kobold.host.EventHandler

import com.codahale.logula.Logging

class RhinoImpl extends Language[Function,RhinoContext] with Logging {
	val name = "js/rhino"

  private val cf: TimingContextFactory =
    new TimingContextFactory()

  private val wrapFactory: DefaultSecureWrapFactory =
		new DefaultSecureWrapFactory()
  wrapFactory.addAllowedNatives(
      classOf[IBase], classOf[IObject], classOf[IUnknown],
      classOf[ICreature],
      classOf[IArea], //Item.class, IModule.class, IPlaceable.class,
      classOf[IPersistency], classOf[ILocation],
      classOf[IVector2], classOf[IVector3],
      classOf[ITask],
      //ISystem.class,
      classOf[IScriptEventRegistry[_]]
  )
	
  private val classShutter: SecureClassShutter =
    new SecureClassShutter(wrapFactory)
  classShutter.addAllowedStartsWith("es.elv.kobold.api.")
  classShutter.addAllowedStartsWith("es.elv.kobold.game.")
	classShutter.addAllowedStartsWith("es.elv.kobold.lang.rhino.")

	private def getContext(host: IObject, ctx: RhinoContext): JSCtx = {
    val jsctx = cf.enterContext(ctx.remainingRuntime)
    jsctx.setWrapFactory(wrapFactory)
		jsctx.setClassShutter(classShutter)
		
		//script.getScope().put("MODULE", script.getScope(), new NModule())
		ctx.scope.put("host", ctx.scope, JSCtx.javaToJS(host, ctx.scope))
		ctx.scope.put("script", ctx.scope, new ScriptEventRegistryImpl(ctx))

    jsctx
	}

  def prepare(source: String): RhinoContext = {
		try {
			val ctx = JSCtx.enter()
			val scope = SecureScriptRuntime.initSecureStandardObjects(ctx, null, true)
			val s = ctx.compileString(source, "", 0, null)
			
			new RhinoContext(this, scope, s)
		} finally {
			JSCtx.exit()
		}
  }

  def execute(obj: IObject, ctx: RhinoContext) = {		
		try {
			val jsctx = getContext(obj, ctx)
      ctx.compiled.exec(jsctx, ctx.scope)
		} finally {
			JSCtx.exit
		}
	}


  def executeEventHandler(obj: IObject, ctx: RhinoContext,
      eh: EventHandler[Function], va: List[Object]) = {
		try {
			val jsctx = getContext(obj, ctx)
			
			val thisObj = jsctx.getWrapFactory().wrapAsJavaObject(jsctx, ctx.scope,
					obj, obj.getClass)
			
      val va2 = va map { JSCtx.javaToJS(_, ctx.scope) } toArray
        
      eh.getHandler.call(jsctx, ctx.scope, thisObj, va2)

		} finally {
			JSCtx.exit
		}
  }
}

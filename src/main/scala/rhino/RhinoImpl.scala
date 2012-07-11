package es.elv.kobold.lang.rhino

import org.mozilla.javascript.{Context => JSCtx}
import org.mozilla.javascript.DefaultSecureWrapFactory
import org.mozilla.javascript.Function
import org.mozilla.javascript.Script
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.SecureClassShutter
import org.mozilla.javascript.SecureScriptRuntime
import org.mozilla.javascript.TimeoutError
import org.mozilla.javascript.TimingContextFactory

import es.elv.kobold.script._
import es.elv.kobold.api._

class RhinoImpl extends Language[Function,RhinoContext] {
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
      //ITask.class,
      //ISystem.class,
      classOf[IScriptEventRegistry[_]]
  )
	
  private val classShutter: SecureClassShutter =
    new SecureClassShutter(wrapFactory)
  classShutter.addAllowedStartsWith("es.elv.kobold.intf.")
  classShutter.addAllowedStartsWith("es.elv.kobold.impl.intf.")
	classShutter.addAllowedStartsWith("es.elv.kobold.lang.rhino.")

	private def getContext(host: IObject, ctx: RhinoContext): JSCtx = {
		val jsctx = cf.enterContext(150)
		jsctx.setWrapFactory(wrapFactory)
		jsctx.setClassShutter(classShutter)
		
		//script.getScope().put("MODULE", script.getScope(), new NModule())
		ctx.scope.put("host", ctx.scope, JSCtx.javaToJS(host, ctx.scope))
		ctx.scope.put("script", ctx.scope, new ScriptEventRegistryImpl(ctx))

    jsctx
	}

  def prepare(host: Host, source: String): RhinoContext = {
		val start = System.currentTimeMillis()
		
		try {
			val ctx = JSCtx.enter()
			val scope = SecureScriptRuntime.initSecureStandardObjects(ctx, null, true)
			val s = ctx.compileString(source, "", 0, null)
			
			new RhinoContext(this, scope, s)
		} finally {
			JSCtx.exit()
			
			val end = System.currentTimeMillis()
			//log.debug("Verify in " + (end - start) + "ms")
		}
  }

  def execute(host: Host, obj: IObject, ctx: RhinoContext) = {		
		val start = System.currentTimeMillis()
		
		try {
			val jsctx = getContext(obj, ctx)

			try {
				ctx.compiled.exec(jsctx, ctx.scope)
			} catch {
        case tmi: TimeoutError => throw new Exception("TMI")
			}
			
		} finally {
			JSCtx.exit
			
			val end = System.currentTimeMillis()
			// log.debug("Script in " + (end - start) + "ms")
		}
	}


  def executeEventHandler(host: Host, obj: IObject, ctx: RhinoContext,
      eh: EventHandler[Function], va: List[Object]) = {

    val start = System.currentTimeMillis()
		
		try {
			val jsctx = getContext(obj, ctx)
			
			val thisObj = jsctx.getWrapFactory().wrapAsJavaObject(jsctx, ctx.scope,
					obj, obj.getClass)
			
			try {
				val va2 = va map { JSCtx.javaToJS(_, ctx.scope) } toArray
        
        /*new Array[Object](va.length)
        va map for (i <- 0 until va.length) {
					va2(i) = JSCtx.javaToJS(va(i), ctx.scope)
        }*/
				
        eh.getHandler.call(jsctx, ctx.scope, thisObj, va2)
				
			} catch {
        case tmi: TimeoutError => throw new Exception("TMI")
			}
			
		} finally {
			JSCtx.exit
			
			val end = System.currentTimeMillis()
			//log.debug("Event handler in " + (end - start) + "ms")
		}
  }
}

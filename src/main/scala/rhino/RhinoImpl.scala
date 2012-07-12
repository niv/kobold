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
import es.elv.kobold.game.System

import com.codahale.logula.Logging

class RhinoImpl extends Language[Function,RhinoContext] with Logging {
  val name = "js/rhino"

  private val cf: TimingContextFactory =
    new TimingContextFactory()

  private val wrapFactory: DefaultSecureWrapFactory =
    new DefaultSecureWrapFactory()
  wrapFactory.addAllowedNatives(
      // Script Context and Helpers
      classOf[IContext[_]], classOf[IContextStore], classOf[IContextAccounting],
      classOf[ISystem],

      // Game object interfaces
      classOf[IBase], classOf[IObject], classOf[IUnknown],
      classOf[ICreature],
      classOf[IArea], //Item.class, IModule.class, IPlaceable.class,
      classOf[ILocation], classOf[IVector2], classOf[IVector3],

      // Tasking
      classOf[ITask]
  )

  private val classShutter: SecureClassShutter =
    new SecureClassShutter(wrapFactory)
  classShutter.addAllowedStartsWith("es.elv.kobold.api.")
  classShutter.addAllowedStartsWith("es.elv.kobold.game.")
  classShutter.addAllowedStartsWith("es.elv.kobold.lang.rhino.")

  private def withContext[T](o: IObject, ctx: RhinoContext)
      (c: (JSCtx) => T): T = try {

    val quota = ctx.quotaSingle
    val jsctx = cf.enterContext(quota)

    jsctx.setWrapFactory(wrapFactory)
    jsctx.setClassShutter(classShutter)

    ctx.scope.put("ctx", ctx.scope, ctx)
    ctx.scope.put("system", ctx.scope, System)

    c(jsctx)

  } finally JSCtx.exit

  def prepare(source: String): RhinoContext = try {
    val ctx = JSCtx.enter()
    val scope = SecureScriptRuntime.initSecureStandardObjects(ctx, null, true)
    val s = ctx.compileString(source, "", 0, null)

    new RhinoContext(this, scope, s)
  } finally JSCtx.exit()

  def execute(obj: IObject, ctx: RhinoContext) =
    withContext(obj, ctx) { c =>
      ctx.compiled.exec(c, ctx.scope)
    }


  def executeEventHandler(obj: IObject, ctx: RhinoContext,
      eh: EventHandler[Function], va: List[Object]) =
    withContext(obj, ctx) { jsctx =>
      val thisObj = jsctx.getWrapFactory().wrapAsJavaObject(jsctx, ctx.scope,
          obj, obj.getClass)

      val va2 = va map { JSCtx.javaToJS(_, ctx.scope) } toArray

      eh.getHandler.call(jsctx, ctx.scope, thisObj, va2)
    }

}

package es.elv.kobold.lang.rhino

// overriding local versions with tweaks.
import org.mozilla.javascript.{DefaultSecureWrapFactory,
  SecureClassShutter, SecureScriptRuntime}

import org.mozilla.javascript.{Context => JSCtx, ContextFactory}
import org.mozilla.javascript.Function
import org.mozilla.javascript.Script
import org.mozilla.javascript.Scriptable

import es.elv.kobold.api._
import es.elv.kobold.host.Language
import es.elv.kobold.host.EventHandler
import es.elv.kobold.host.Accounting

import com.codahale.logula.Logging

class RhinoImpl extends Language[Function,RhinoContext] with Logging {
  val name = "js/rhino"

  private val wrapFactory: DefaultSecureWrapFactory =
    new DefaultSecureWrapFactory()
  wrapFactory.addAllowedNatives(
      // Script Context and Helpers
      classOf[IContext[_]], classOf[IContextStore], classOf[IContextAccounting],

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

  private def withContext[T](c: (JSCtx) => T)
      (implicit ctx: RhinoContext): T = try {

    val quota = ctx.quotaSingle
    val jsctx = ContextFactory.getGlobal.enterContext

    jsctx.setWrapFactory(wrapFactory)
    jsctx.setClassShutter(classShutter)

    ctx.scope.put("ctx", ctx.scope, ctx)

    c(jsctx)

  } finally JSCtx.exit

  def prepare(source: java.io.InputStream): RhinoContext = try {
    val src = io.Source.fromInputStream(source).mkString("")
    val ctx = JSCtx.enter()
    val scope = SecureScriptRuntime.initSecureStandardObjects(ctx, null, true)
    val s = ctx.compileString(src, "", 0, null)

    implicit val rctx = new RhinoContext(this, scope, s)
    withContext { jsctx =>
      s.exec(jsctx, scope)
    }
    rctx
  } finally JSCtx.exit()

  def executeEventHandler(obj: IObject, eh: EventHandler[Function],
      va: List[Object])(implicit ctx: RhinoContext) =
    withContext { jsctx =>
      val thisObj = jsctx.getWrapFactory().wrapAsJavaObject(jsctx, ctx.scope,
          obj, obj.getClass)

      val va2 = va map { JSCtx.javaToJS(_, ctx.scope) } toArray

      Accounting.enforceQuota {
        eh.getHandler.call(jsctx, ctx.scope, thisObj, va2)
      }
    }

}

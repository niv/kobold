package es.elv.kobold.lang.rhino

// overriding local versions with tweaks.
import org.mozilla.javascript.{SecureClassShutter,
  SecureScriptRuntime, WrapFactory, SecureWrapFactory}

import org.mozilla.javascript.{Context => JSCtx, ContextFactory, ContextAction}
import org.mozilla.javascript.Function
import org.mozilla.javascript.Script
import org.mozilla.javascript.Scriptable

import es.elv.kobold.api._
import es.elv.kobold.host.Language
import es.elv.kobold.host.EventHandler
import es.elv.kobold.host.Accounting

import com.codahale.logula.Logging

class RhinoImpl extends Language[Function,RhinoContext,JSCtx] with Logging {
  val name = "js/rhino"

 private val wrapFactory: WrapFactory with SecureWrapFactory =
    new ScalaSecureWrapFactory()

  wrapFactory.addAllowedNatives(
      // Script Context and Helpers
      classOf[IContext[_]], classOf[IConcurrency[_]],
      classOf[IContextStore], classOf[IContextAccounting],

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

   private val contextFactory: ContextFactory = new ContextFactory {
    override def makeContext: JSCtx = {
      val c = super.makeContext
      c.setWrapFactory(wrapFactory)
      c.setClassShutter(classShutter)
      c
    }
  }

  def inLanguage[T](c: (JSCtx) => T)
      (implicit ctx: RhinoContext): T = {

    contextFactory.call(new ContextAction {
      def run(jsctx: JSCtx): Object = {
        ctx.scope.put("ctx", ctx.scope, ctx)
        c(jsctx).asInstanceOf[Object]
      }
    }).asInstanceOf[T]
  }

  def prepare(source: java.io.InputStream): RhinoContext = try {
    val ctx = JSCtx.enter
    ctx.setLanguageVersion(JSCtx.VERSION_1_7)
    ctx.setOptimizationLevel(9)

    val scope = SecureScriptRuntime.initSecureStandardObjects(ctx, null, true)
    val s = ctx.compileReader(new java.io.InputStreamReader(source),
      "", 0, null)
    implicit val rctx = new RhinoContext(this, scope, s)

    println(ctx.getLanguageVersion)
    inLanguage { jsctx =>
      s.exec(jsctx, scope)
    }
    rctx
  } finally JSCtx.exit()

  /*
  def executeEventHandler(eh: EventHandler[Function])
      (implicit ctx: RhinoContext) =
    executeEventHandler(Module(), eh, List())
  */

  def executeEventHandler(obj: IObject, eh: Function,
      va: List[Object])(implicit ctx: RhinoContext) =
    inLanguage { jsctx =>
      val thisObj = jsctx.getWrapFactory().wrapAsJavaObject(jsctx, ctx.scope,
          obj, obj.getClass)

      val va2 = va map { JSCtx.javaToJS(_, ctx.scope) } toArray

      Accounting.enforceQuota {
        eh.call(jsctx, ctx.scope, thisObj, va2)
      }(ctx, this)
    }

}

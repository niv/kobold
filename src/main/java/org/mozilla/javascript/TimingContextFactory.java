package org.mozilla.javascript;


import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

public class TimingContextFactory extends ContextFactory {

	@SuppressWarnings("deprecation")
	class MyContext extends Context {
		protected long timeout;
		protected long startTime;
    };

	public Context enterContext(long timeout) {
		MyContext ctx = (MyContext) super.enterContext();
		
		ctx.timeout = timeout;
		return ctx;
	}	
    
    @Override
    protected Context makeContext() {
    	Context ctx = new MyContext();
    	ctx.setInstructionObserverThreshold(10000);
    	return ctx;
    };
    
	protected void observeInstructionCount(Context cx, int instructionCount) {
         MyContext mcx = (MyContext)cx;
         long currentTime = System.currentTimeMillis();
         if (currentTime - mcx.startTime > mcx.timeout) {
             // More then x ms from Context creation time:
             // it is time to stop the script.
             // Throw Error instance to ensure that script will never
             // get control back through catch or finally.
             throw new ScriptTimeoutError();
         }
    };
    
    @Override
    protected Object doTopCall(Callable callable, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		MyContext mcx = (MyContext)cx;
		mcx.startTime = System.currentTimeMillis();
		 
		return super.doTopCall(callable, cx, scope, thisObj, args);
	}
}
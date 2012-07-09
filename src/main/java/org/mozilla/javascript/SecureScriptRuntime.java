package org.mozilla.javascript;

import java.util.Arrays;
import java.util.List;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.ClassCache;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeBoolean;
import org.mozilla.javascript.NativeCall;
import org.mozilla.javascript.NativeDate;
import org.mozilla.javascript.NativeError;
import org.mozilla.javascript.NativeGlobal;
import org.mozilla.javascript.NativeIterator;
import org.mozilla.javascript.NativeMath;
import org.mozilla.javascript.NativeNumber;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.NativeScript;
import org.mozilla.javascript.NativeString;
import org.mozilla.javascript.NativeWith;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class SecureScriptRuntime {
	private static final Object LIBRARY_SCOPE_KEY = "LIBRARY_SCOPE";
	
	public static enum Feature {
		OBJECT,
		FUNCTION,
		GLOBAL,
		ARRAY,
		ERROR, // implies GLOBAL
		DATE,
		MATH,
		WITH,
		CALL,
		SCRIPT,
		ITERATOR
	};
	
	public static ScriptableObject initSecureStandardObjects(Context cx, ScriptableObject scope, boolean sealed) {
		return initStandardObjects(cx, scope, sealed,
				Feature.OBJECT,
				Feature.FUNCTION,
				Feature.GLOBAL,
				Feature.ARRAY,
				Feature.ERROR,
				Feature.DATE,
				Feature.MATH,
				Feature.WITH
		);
	}
	
	public static ScriptableObject initStandardObjects(Context cx, ScriptableObject scope, boolean sealed, Feature... features) {
		List<Feature> featlist = Arrays.asList(features);
		
		if (scope == null) {
			scope = new NativeObject();
		}
		scope.associateValue(LIBRARY_SCOPE_KEY, scope);
		(new ClassCache()).associate(scope);

		if (featlist.contains(Feature.FUNCTION))
			BaseFunction.init(scope, sealed);
		if (featlist.contains(Feature.OBJECT))
			NativeObject.init(scope, sealed);

		Scriptable objectProto = ScriptableObject.getObjectPrototype(scope);

		// Function.prototype.__proto__ should be Object.prototype
		Scriptable functionProto = ScriptableObject.getFunctionPrototype(scope);
		functionProto.setPrototype(objectProto);

		// Set the prototype of the object passed in if need be
		if (scope.getPrototype() == null)
			scope.setPrototype(objectProto);

		// must precede NativeGlobal since it's needed therein
		if (featlist.contains(Feature.GLOBAL) || featlist.contains(Feature.ERROR))
			NativeError.init(scope, sealed);
		
		if (featlist.contains(Feature.GLOBAL))
			NativeGlobal.init(cx, scope, sealed);
		
		if (featlist.contains(Feature.ARRAY)) {
			NativeArray.init(scope, sealed);
			if (cx.getOptimizationLevel() > 0) {
				// When optimizing, attempt to fulfill all requests for new Array(N)
				// with a higher threshold before switching to a sparse
				// representation
				NativeArray.setMaximumInitialCapacity(200000);
			}
		}
		
		NativeString.init(scope, sealed);
		
		NativeBoolean.init(scope, sealed);
		
		NativeNumber.init(scope, sealed);
		
		if (featlist.contains(Feature.DATE))
			NativeDate.init(scope, sealed);
		if (featlist.contains(Feature.MATH))
			NativeMath.init(scope, sealed);

		if (featlist.contains(Feature.WITH))
			NativeWith.init(scope, sealed);
		if (featlist.contains(Feature.CALL))
			NativeCall.init(scope, sealed);
		if (featlist.contains(Feature.SCRIPT))
			NativeScript.init(scope, sealed);

		if (featlist.contains(Feature.ITERATOR))
			NativeIterator.init(scope, sealed); // Also initializes NativeGenerator

		return scope;
	}
}

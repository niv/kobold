package org.mozilla.javascript;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class DefaultSecureWrapFactory extends WrapFactory implements SecureWrapFactory {
	
	private final Map<Class<?>, List<String>> allowedNative =
		Collections.synchronizedMap(new HashMap<Class<?>, List<String>>());
	
	@Override
	public Map<Class<?>, List<String>> getAllowedNativeInterfaces() {
		Map<Class<?>, List<String>> cp =
			new HashMap<Class<?>, List<String>>();

		for (Entry<Class<?>, List<String>> entry : allowedNative.entrySet()) {
			List<String> cplist = new LinkedList<String>();
			cplist.addAll(entry.getValue());
			cp.put(entry.getKey(), cplist);
		}
		
		return cp;
	}
	
	@Override
	public void addAllowedNatives(Class<?>... klasses) {
		for (Class<?> klass : klasses) {
			List<String> allowedProps = new LinkedList<String>();
			
			if (!klass.isInterface())
				throw new IllegalArgumentException("AllowedNative class must be a interface: " + klass);
			
			for (Method m : klass.getDeclaredMethods())
				if (Modifier.isPublic(m.getModifiers()))
					allowedProps.add(m.getName());
			
			addAllowedNative(klass, allowedProps);
		}
	}
	
	@Override
	public void addAllowedNative(Class<?> klass, List<String> exposedMethods) {
		if (!klass.isInterface())
			throw new IllegalArgumentException("AllowedNative class must be a interface: " + klass);

		if (allowedNative.containsKey(klass))
			throw new RuntimeException("Interface " + klass + " was already registered as allowed.");
		allowedNative.put(klass, exposedMethods);
	}
	

	@Override
	public void addAllowedNative(Class<?> klass, String... exposedMethods) {
		List<String> asList = Arrays.asList(exposedMethods);
		addAllowedNative(klass, asList);
	}
	
	@Override
	public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType) {
		return new SandboxedNativeJavaObject(this, scope, javaObject, staticType);
	}

	
	private boolean isAllowedMethodCall(Object obj, String method, Class<?> cl) {
		if (allowedNative.containsKey(cl) && (allowedNative.get(cl).contains(method)))
			return true;
		
		for (Class<?> intf : cl.getInterfaces())
			if (isAllowedMethodCall(obj, method, intf))
				return true;
		
		if (cl.getSuperclass() != null)
			if (isAllowedMethodCall(obj, method, cl.getSuperclass()))
				return true;
		
		return false;
	}
	
	@Override
	public boolean isAllowedMethodCall(Object obj, String method) {
		return isAllowedMethodCall(obj, method,  obj.getClass());
	}

	@Override
	public Object wrap(Context cx, Scriptable scope, Object obj, Class<?> staticType) {
		if (obj instanceof String || obj instanceof Boolean || obj instanceof Number)
			return Context.javaToJS(obj, scope);
		
		else if (obj != null && obj.getClass().isArray()) {
			// Is this really neccessary?
			Object[] oa = (Object[]) obj;
			Scriptable array = cx.newArray(scope, oa.length);
			for (int i = 0; i < oa.length; i++)
				// array.put(i, array, Context.javaToJS(oa[i], scope));
				array.put(i, array, wrap(cx, scope, oa[i], oa[i].getClass()));
			return array;
			
		} else
			return super.wrap(cx, scope, obj, staticType);
	}


}
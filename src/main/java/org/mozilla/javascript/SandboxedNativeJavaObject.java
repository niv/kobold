package org.mozilla.javascript;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;


public class SandboxedNativeJavaObject extends NativeJavaObject {
	private static final long serialVersionUID = 1361913806485993434L;

	private final SecureWrapFactory swf;

	public SandboxedNativeJavaObject(SecureWrapFactory swf, Scriptable scope, Object javaObject, Class<?> staticType) {
		super(scope, javaObject, staticType);
		this.swf = swf;
	}
	
	@Override
	public boolean has(String name, Scriptable start) {
		if (swf.isAllowedMethodCall(javaObject, name))
			return super.has(name, start);
		else
			return false;
	}
		
	@Override
	public Object get(String name, Scriptable start) {
		if (!has(name, start))
			return NOT_FOUND;
		
		else
			return super.get(name, start);
	}
	
	
	@Override
	public Object get(int index, Scriptable start) {
		return NOT_FOUND;
	}
	
	@Override
	public boolean has(int index, Scriptable start) {
		return false;
	}
	
	@Override
	public void put(int index, Scriptable start, Object value) {
		throw new UnsupportedOperationException("Cannot modify object.");
	}
	
	@Override
	public void put(String name, Scriptable start, Object value) {
		throw new UnsupportedOperationException("Cannot modify object.");
	}
	
	@Override
	public void delete(int index) {
		throw new UnsupportedOperationException("Cannot modify object.");
	}
	@Override
	public void delete(String name) {
		throw new UnsupportedOperationException("Cannot modify object.");
	}
}
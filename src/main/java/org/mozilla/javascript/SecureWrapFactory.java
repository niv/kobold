package org.mozilla.javascript;

import java.util.List;
import java.util.Map;

public interface SecureWrapFactory {
	/**
	 * Adds the given interfaces(es) to the list of allowed Classes.
	 * 
	 * Will add ALL public methods as accessible to the js interpreter.
	 * 
	 * Will NOT walk superclasses or interfaces. You have to explicitly
	 * give all classes you want exposed.
	 */
	public void addAllowedNatives(Class<?>... klasses);
	
	/**
	 * Adds the given interface to the list of allowed Classes.
	 * 
	 * Will add only the given method names as accessible to the js interpreter.
	 *  
	 * Will NOT walk superclasses or interfaces. You have to explicitly
	 * give all classes you want exposed.
	 */
	public void addAllowedNative(Class<?> klass, List<String> exposedMethods);
	
	/**
	 * Adds the given interface to the list of allowed Classes.
	 * 
	 * Will add only the given method names as accessible to the js interpreter.
	 * 
	 * Will not allow any method access when called with no method arguments. Take
	 * care not to confuse with addAllowedNatives().
	 *  
	 * Will NOT walk superclasses or interfaces. You have to explicitly
	 * give all classes you want exposed.
	 * 
	 * This method aliases to addAllowedNative(klass, List<String>);
	 */
	public void addAllowedNative(Class<?> klass, String... exposedMethods);
	
	
	/**
	 * Will return true if the given method name is allowed to be called
	 * from javascript on the given specific object.
	 */
	public boolean isAllowedMethodCall(Object obj, String method);
	
	/**
	 * Returns a Map describing all allowed interface classes &
	 * method names.
	 */
	public Map<Class<?>, List<String>> getAllowedNativeInterfaces();
}

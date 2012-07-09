package org.mozilla.javascript;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SecureClassShutter implements ClassShutter {
	final private List<String> allowedStartsWith =
		Collections.synchronizedList(new LinkedList<String>());

	
	public SecureClassShutter(SecureWrapFactory wf, Class<?>... allowedClasses) {		
		for (Class<?> klass : wf.getAllowedNativeInterfaces().keySet())
			allowedStartsWith.add(klass.getName());
	}
	
	@Override
	public boolean visibleToScripts(String fullClassName) {
		for (String sw : allowedStartsWith)
			if (fullClassName.startsWith(sw))
				return true;
		
		return false;
	}
	
	public void addAllowedStartsWith(String startsWith) {
		allowedStartsWith.add(startsWith);
	}
}

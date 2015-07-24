package javax.arang.spring.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.InvocationHandler;

public class UppercaseHandler implements InvocationHandler {

	Object target;
	
	public UppercaseHandler(Object target) {
		this.target = target;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object ret = method.invoke(proxy, args);
		if (ret instanceof String) {
			return ((String)ret).toUpperCase();
		} else {
			return ret;
		}
	}

}

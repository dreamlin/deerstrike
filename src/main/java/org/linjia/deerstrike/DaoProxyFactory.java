package org.linjia.deerstrike;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import org.linjia.deerstrike.pi.ITableDao;

public class DaoProxyFactory {
	
	public static <T extends ITableDao> T createProxy(Class<T> clazz) {
        MethodInterceptor methodInterceptor = new DaoMethodInterceptor();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(methodInterceptor);
        return (T)enhancer.create();
	}
	
}

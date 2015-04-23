package org.linjia.deerstrike;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.linjia.deerstrike.model.AopParameter;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public class AopMethod {
	protected final Logger logger = LogManager.getLogger(AopMethod.class);
	private String className;
	private String methodName;
	private List<AopParameter> aopParameters;

	public AopMethod(Method method) {
		Class clazz = method.getDeclaringClass();
		this.className = method.getDeclaringClass().getName();
		this.methodName = method.getName();
		aopParameters = new ArrayList<AopParameter>();
		// 获取方法的参数类型
		Class<?>[] types = method.getParameterTypes();
		if (types.length > 0) {
			try {
				// 获取方法的参数名称
				String[] methodParamNames = this.getMethodParamNames(clazz,
						this.methodName);
				int ix = 0;
				for (String flagString : methodParamNames) {
					aopParameters.add(new AopParameter(flagString, types[ix]));
					ix++;
				}
			} catch (NotFoundException e) {
				logger.warn(
						"获取方法参数名时出错,类名：" + className + " 方法名：" + methodName, e);
			}
		}
	}

	/**
	 * 取类名
	 * 
	 * @return
	 */
	public String getOwnerClassName() {
		return this.className;
	}

	/**
	 * 取方法名
	 * 
	 * @return
	 */
	public String getOwnerMethodName() {
		return this.methodName;
	}

	/**
	 * 取所有参数
	 * 
	 * @return
	 */
	public List<AopParameter> getParameters() {
		return this.aopParameters;
	}

	/**
	 * 设置参数的值
	 * 
	 * @param ix
	 * @param value
	 */
	public void setParameter(int ix, Object value) {
		this.aopParameters.get(ix).setValue(value);
	}

	/**
	 * 获取参数信息的代码在此类实现
	 * 
	 * @param clazz
	 * @param method
	 * @return
	 * @throws NotFoundException
	 */
	protected String[] getMethodParamNames(Class clazz, String method)
			throws NotFoundException {
		ClassPool pool = ClassPool.getDefault();
		pool.insertClassPath(new ClassClassPath(this.getClass()));
		CtClass cc = pool.get(clazz.getName());
		CtMethod cm = cc.getDeclaredMethod(method);
		return getMethodParamNames(cm);
	}

	protected String[] getMethodParamNames(CtMethod cm)
			throws NotFoundException {
		CtClass cc = cm.getDeclaringClass();
		MethodInfo methodInfo = cm.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
				.getAttribute(LocalVariableAttribute.tag);
		if (attr == null)
			throw new NotFoundException(cc.getName());

		String[] paramNames = new String[cm.getParameterTypes().length];
		int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
		for (int i = 0; i < paramNames.length; i++) {
			paramNames[i] = attr.variableName(i + pos);
		}
		return paramNames;
	}
}
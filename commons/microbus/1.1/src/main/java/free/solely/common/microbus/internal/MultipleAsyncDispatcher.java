package free.solely.common.microbus.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.Modifier;
import free.solely.common.microbus.Subscriber;

/**
 * @author shanguoming 2014年11月24日 下午3:16:16
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年11月24日 下午3:16:16
 */
public class MultipleAsyncDispatcher extends SyncDispatcher {
	
	private Executor executor = null;
	
	protected Subscriber createJavassistBytecodeDynamicProxy(Class<?> clazz, Sender sender, Object sub, Method m) throws Exception {
		ClassPool pool = ClassPool.getDefault();
		CtClass proxy = pool.makeClass(clazz.getName() + "$javassist-" + UUID.randomUUID().toString());
		CtClass superclass = pool.get(clazz.getName());
		proxy.setSuperclass(superclass);
		proxy.addInterface(pool.get(Observer.class.getName()));
		// 回调对象参数
		CtField futureField = new CtField(pool.get(FutureTask.class.getName()), "future", proxy);
		futureField.setModifiers(Modifier.PRIVATE);
		proxy.addField(futureField);
		// 被代理类参数
		CtField subField = new CtField(pool.get(sub.getClass().getName()), "sub", proxy);
		subField.setModifiers(Modifier.PUBLIC);
		proxy.addField(subField);
		// 被代理方法参数
		CtField methodField = new CtField(pool.get(m.getClass().getName()), "method", proxy);
		methodField.setModifiers(Modifier.PUBLIC);
		proxy.addField(methodField);
		// 线程池参数
		CtField executorField = new CtField(pool.get(Executor.class.getName()), "executor", proxy);
		executorField.setModifiers(Modifier.PUBLIC);
		proxy.addField(executorField);
		/* 创建构造方法以便注入拦截器 */
		CtConstructor cc = new CtConstructor(new CtClass[] {pool.get(Observable.class.getName())}, proxy);
		cc.setBody("{super($1);}");
		proxy.addConstructor(cc);
		StringBuilder getResult = new StringBuilder();
		getResult.append("public Object getResult(long timeout, ");
		getResult.append(TimeUnit.class.getName());
		getResult.append(" time) {");
		getResult.append("try{");
		getResult.append("if(null != future){");
		getResult.append("return future.get(timeout, time);");
		getResult.append("}");
		getResult.append("}catch(Exception e){");
		getResult.append("addError(new free.solely.common.microbus.ConsumeException(e));");
		getResult.append("}");
		getResult.append("return null;");
		getResult.append("}");
		proxy.addMethod(CtNewMethod.make(getResult.toString(), proxy));
		StringBuilder update = new StringBuilder();
		update.append("public void update(");
		update.append(Observable.class.getName());
		update.append(" observable, Object args) {");
		update.append("try{");
		update.append("future = createFutureTask(sub,method,args);");
		update.append("executor.execute(future);");
		update.append("}catch(Exception e){");
		update.append("addError(new free.solely.common.microbus.ConsumeException(e));");
		update.append("}");
		update.append("}");
		proxy.addMethod(CtNewMethod.make(update.toString(), proxy));
		Class<?> pc = proxy.toClass();
		Constructor<?> constructor = pc.getConstructor(Observable.class);
		Object bytecodeProxy = constructor.newInstance(sender);
		Field sfiled = bytecodeProxy.getClass().getField("sub");
		sfiled.setAccessible(true);
		sfiled.set(bytecodeProxy, sub);
		Field mfiled = bytecodeProxy.getClass().getField("method");
		mfiled.setAccessible(true);
		mfiled.set(bytecodeProxy, m);
		Field efiled = bytecodeProxy.getClass().getField("executor");
		efiled.setAccessible(true);
		efiled.set(bytecodeProxy, getExecutor());
		return (Subscriber)bytecodeProxy;
	}
	
	/**
	 * 如果executor为null则返回默认线程池（Executors.newCachedThreadPool()）
	 * @author shanguoming 2014年11月24日 下午2:53:05
	 * @return
	 * @modify: {原因} by shanguoming 2014年11月24日 下午2:53:05
	 */
	public Executor getExecutor() {
		if (null == executor) {
			return Executors.newCachedThreadPool();
		}
		return executor;
	}
	
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}
}

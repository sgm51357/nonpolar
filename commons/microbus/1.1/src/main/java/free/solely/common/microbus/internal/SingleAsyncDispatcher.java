package free.solely.common.microbus.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.Modifier;
import free.solely.common.microbus.MultipleQueueException;
import free.solely.common.microbus.Subscriber;
import free.solely.common.microbus.annotation.Queue;
import free.solely.common.microbus.annotation.Topic;

/**
 * @author shanguoming 2014年11月27日 上午8:36:44
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年11月27日 上午8:36:44
 */
public class SingleAsyncDispatcher extends SyncDispatcher {
	
	/**
	 * @author shanguoming 2014年11月27日 上午9:22:29
	 * @param sub
	 * @throws Exception
	 * @modify: {原因} by shanguoming 2014年11月27日 上午9:22:29
	 */
	@Override
	public void subscribe(Object sub) throws Exception {
		if (null != sub && !(sub instanceof Class<?>)) {
			Class<?> clazz = sub.getClass();
			Method[] methods = clazz.getMethods();
			if (methods != null && methods.length > 0) {
				for (Method m : methods) {
					Topic t = m.getAnnotation(Topic.class);
					Queue q = m.getAnnotation(Queue.class);
					if (t != null) {
						if (null != t.name() && !"".equals(t.name().trim())) {
							AsyncSender topic = (AsyncSender)topics.get(t.name().trim());
							if (topic == null) {
								topic = new AsyncSender(t.max());
								topics.put(t.name().trim(), topic);
							}
							topic.addObserver(createJavassistBytecodeDynamicProxy(AsyncSubscriber.class, topic, sub, m));
						}
					}
					if (q != null) {
						if (null != q.name() && !"".equals(q.name().trim())) {
							AsyncSender queue = (AsyncSender)queues.get(q.name().trim());
							if (queue == null) {
								queue = new AsyncSender(q.max());
								queues.put(q.name().trim(), queue);
							} else {
								throw new MultipleQueueException(clazz.getName() + "申明的" + q.name() + "队列已经被使用！");
							}
							queue.addObserver(createJavassistBytecodeDynamicProxy(AsyncSubscriber.class, queue, sub, m));
						}
					}
				}
			}
		}
	}
	
	protected Subscriber createJavassistBytecodeDynamicProxy(Class<?> clazz, Sender sender, Object sub, Method m) throws Exception {
		ClassPool pool = ClassPool.getDefault();
		CtClass proxy = pool.makeClass(clazz.getName() + "$javassist-" + UUID.randomUUID().toString());
		CtClass superclass = pool.get(clazz.getName());
		proxy.setSuperclass(superclass);
		proxy.addInterface(pool.get(Observer.class.getName()));
		// 被代理类参数
		CtField subField = new CtField(pool.get(sub.getClass().getName()), "sub", proxy);
		subField.setModifiers(Modifier.PUBLIC);
		proxy.addField(subField);
		// 被代理方法参数
		CtField methodField = new CtField(pool.get(m.getClass().getName()), "method", proxy);
		methodField.setModifiers(Modifier.PUBLIC);
		proxy.addField(methodField);
		/* 创建构造方法以便注入拦截器 */
		CtConstructor cc = new CtConstructor(new CtClass[] {pool.get(Observable.class.getName())}, proxy);
		cc.setBody("{super($1);}");
		proxy.addConstructor(cc);
		StringBuilder update = new StringBuilder();
		update.append("public void update(");
		update.append(Observable.class.getName());
		update.append(" observable, Object args) {");
		update.append("execute(sub, method, args);");
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
		return (Subscriber)bytecodeProxy;
	}
}

package free.solely.common.microbus.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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
 * @author shanguoming 2014年11月24日 上午11:38:59
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年11月24日 上午11:38:59
 */
public class SyncDispatcher {
	
	protected ConcurrentHashMap<String, Sender> topics = new ConcurrentHashMap<String, Sender>();
	protected ConcurrentHashMap<String, Sender> queues = new ConcurrentHashMap<String, Sender>();
	
	public List<Subscriber> topic(String name, Object... args) {
		if (null != name && !"".equals(name.trim())) {
			Sender topic = topics.get(name.trim());
			if (null != topic) {
				if (args != null) {
					topic.push(args);
					return topic.getSubscribers();
				} else {
					throw new NullPointerException("args参数空指针异常");
				}
			}
		}
		return null;
	}
	
	public Subscriber queue(String name, Object... args) {
		if (null != name && !"".equals(name.trim())) {
			Sender queue = queues.get(name.trim());
			if (null != queue) {
				if (args != null) {
					queue.push(args);
					List<Subscriber> subscribers = queue.getSubscribers();
					if (null != subscribers && !subscribers.isEmpty()) {
						return subscribers.get(0);
					}
				} else {
					throw new NullPointerException("args参数空指针异常");
				}
			}
		}
		return null;
	}
	
	public List<Subscriber> publish(String name, Object... args) {
		List<Subscriber> subscribers = topic(name, args);
		Subscriber sub = queue(name, args);
		if (sub != null) {
			if (subscribers == null) {
				subscribers = new ArrayList<Subscriber>();
			}
			subscribers.add(sub);
		}
		return subscribers;
	}
	
	/**
	 * 消费事件注册
	 * @author shanguoming 2014年11月27日 下午7:30:40
	 * @param sub
	 * @throws Exception
	 * @modify: {原因} by shanguoming 2014年11月27日 下午7:30:40
	 */
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
							Sender topic = topics.get(t.name().trim());
							if (topic == null) {
								topic = new Sender();
								topics.put(t.name().trim(), topic);
							}
							topic.addObserver(createJavassistBytecodeDynamicProxy(Subscriber.class, topic, sub, m));
						}
					}
					if (q != null) {
						if (null != q.name() && !"".equals(q.name().trim())) {
							Sender queue = queues.get(q.name().trim());
							if (queue == null) {
								queue = new Sender();
								queues.put(q.name().trim(), queue);
							} else {
								throw new MultipleQueueException(clazz.getName() + "申明的" + q.name() + "队列已经被使用！");
							}
							queue.addObserver(createJavassistBytecodeDynamicProxy(Subscriber.class, queue, sub, m));
						}
					}
				}
			}
		}
	}
	
	public void unsubscribe(String name) {
		if (null != name && !"".equals(name.trim())) {
			Sender topic = topics.get(name);
			topic.deleteObservers();
			topics.remove(name);
			Sender queue = queues.get(name);
			queue.deleteObservers();
			queues.remove(name);
		}
	}
	
	public void unTopic(String name) {
		if (null != name && !"".equals(name.trim())) {
			Sender topic = topics.get(name);
			topic.deleteObservers();
			topics.remove(name);
		}
	}
	
	public void unQueue(String name) {
		if (null != name && !"".equals(name.trim())) {
			Sender queue = queues.get(name);
			queue.deleteObservers();
			queues.remove(name);
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
		StringBuilder getResult = new StringBuilder();
		getResult.append("public Object getResult(long timeout, ");
		getResult.append(TimeUnit.class.getName());
		getResult.append(" time) {");
		getResult.append("return super.getResult();");
		getResult.append("}");
		proxy.addMethod(CtNewMethod.make(getResult.toString(), proxy));
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

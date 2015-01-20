package free.solely.common.microbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shanguoming 2014年11月24日 下午1:44:31
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年11月24日 下午1:44:31
 */
public abstract class Subscriber implements Observer {
	
	protected static final Logger log = LoggerFactory.getLogger(Subscriber.class);
	private List<ConsumeException> errors;
	private Object result;
	
	public List<ConsumeException> getErrors() {
		return errors;
	}
	
	public Subscriber(Observable observable) {
		observable.addObserver(this);
	}
	
	public Object getResult() {
		return result;
	}
	
	public abstract Object getResult(long timeout, TimeUnit time);
	
	public void addError(ConsumeException error) {
		if (null != error) {
			if (null == errors) {
				errors = new ArrayList<ConsumeException>();
			}
			if (errors.size() == 10) {
				errors.remove(0);
			}
			errors.add(error);
		}
	}
	
	protected FutureTask<Object> createFutureTask(final Object sub, final Method method, final Object args) {
		return new java.util.concurrent.FutureTask<Object>(new java.util.concurrent.Callable<Object>() {
			
			public Object call() {
				execute(sub, method, args);
				return result;
			}
		});
	}
	
	protected void execute(Object sub, Method method, Object args) {
		try {
			if (args == null) {
				result = method.invoke(sub);
			} else {
				result = method.invoke(sub, (Object[])args);
			}
		} catch (IllegalArgumentException e) {
			addError(new ConsumeException(e));
			log.error("[{}.{}]处理消息异常", sub.getClass().getName(), method.getName(), e);
		} catch (IllegalAccessException e) {
			addError(new ConsumeException(e));
			log.error("[{}.{}]处理消息异常", sub.getClass().getName(), method.getName(), e);
		} catch (InvocationTargetException e) {
			addError(new ConsumeException(e));
			log.error("[{}.{}]处理消息异常", sub.getClass().getName(), method.getName(), e);
		} catch (Exception e) {
			addError(new ConsumeException(e));
			log.error("[{}.{}]处理消息异常", sub.getClass().getName(), method.getName(), e);
		}
	}
}

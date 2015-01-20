package free.solely.common.microbus.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import free.solely.common.microbus.Subscriber;

/**
 * @author shanguoming 2014年11月26日 下午5:53:05
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年11月26日 下午5:53:05
 */
class Sender extends Observable {
	
	private List<Subscriber> subscribers;
	
	@Override
	public synchronized void addObserver(Observer o) {
		super.addObserver(o);
		addSubscriber((Subscriber)o);
	}
	
	@Override
	public synchronized void deleteObserver(Observer o) {
		super.deleteObserver(o);
		deleteSubscriber((Subscriber)o);
	}
	
	@Override
	public synchronized void deleteObservers() {
		super.deleteObservers();
		deleteSubscribers();
	}
	
	public void push(Object... args) {
		setChanged();
		notifyObservers(args);
	}
	
	private void addSubscriber(Subscriber s) {
		if (subscribers == null) {
			subscribers = new ArrayList<Subscriber>();
		}
		subscribers.add(s);
	}
	
	private void deleteSubscriber(Subscriber s) {
		if (subscribers != null) {
			subscribers.remove(s);
		}
	}
	
	private void deleteSubscribers() {
		if (subscribers != null) {
			subscribers.clear();
		}
	}
	
	public List<Subscriber> getSubscribers() {
		return subscribers;
	}
}

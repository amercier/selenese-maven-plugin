package com.github.amercier.selenium.thread;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class ObservableCountDownLatch<T extends Thread> extends CountDownLatch {
	
	protected Set<CountDownLatchListener<T>> listeners;

	public ObservableCountDownLatch(int count) {
		super(count);
		this.listeners = new HashSet<CountDownLatchListener<T>>();
	}
	
	public void addListener(CountDownLatchListener<T> listener) {
		this.listeners.add(listener);
	}
	
	public void removeListener(CountDownLatchListener<T> listener) {
		this.listeners.remove(listener);
	}
	
	@Override
	public void countDown() {
		throw new RuntimeException("ObservableCountDownLatch.countDown() should not be called without any \"item\" parameter.");
	}
	
	public void countDown(T terminated) {
		fireCountedDown(terminated);
		super.countDown();
	}

	protected void fireCountedDown(T terminated) {
		for(CountDownLatchListener<T> listener : listeners) {
			listener.fireCountedDown(this, terminated);
		}
	}
}

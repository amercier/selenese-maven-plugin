package com.github.amercier.selenium.thread;

public interface CountDownLatchListener<T extends Thread> {

	void fireCountedDown(ObservableCountDownLatch<T> latch, T item);

}

package utilities;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Singleton that provides a cached thread pool for the execution of {@link Runnable}s and {@link Callable}s.
 * 
 * @author Gerrit
 * 
 */
public class ExecutorServiceProvider {
	private static ExecutorServiceProvider instance;
	private final ExecutorService myExecutorService;

	/**
	 * Private constructor.
	 */
	private ExecutorServiceProvider() {
		myExecutorService = Executors.newCachedThreadPool();
	}

	/**
	 * Returns the instance of the {@link ExecutorServiceProvider}.
	 * 
	 * @return
	 */
	public synchronized static ExecutorServiceProvider getInstance() {
		if (instance == null) {
			instance = new ExecutorServiceProvider();
		}
		return instance;
	}

	/**
	 * Returns an {@link ExecutorService} that can execute submitted {@link Runnable}s and {@link Callable}s.
	 * Furthermore, it provides methods that can produce a {@link java.util.concurrent.Future} for tracking progress of
	 * one or more asynchronous tasks.
	 * 
	 * @return
	 */
	public synchronized ExecutorService getExecutorService() {
		return this.myExecutorService;
	}
}

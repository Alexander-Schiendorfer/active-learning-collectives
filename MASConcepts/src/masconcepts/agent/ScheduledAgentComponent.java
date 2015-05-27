package masconcepts.agent;

import java.util.Date;
import java.util.HashMap;

import utilities.caching.CachedValue;
import masconcepts.agent.scheduler.ScheduledActionFrequencyException;
import masconcepts.agent.scheduler.TargetedScheduledAction;

/***
 * A {@link ScheduledAgentComponent} is a {@link AgentComponent} that additionally holds a cache for values it
 * calculates frequently.
 * 
 * @author Oliver
 * 
 * @param <T>
 */
public class ScheduledAgentComponent<T extends IMessageHandlingAgent> extends AgentComponent<T> {

	/**
	 * A map holding cached values from previous method calls. Keys is the {@link TargetedScheduledAction} whose return
	 * value is cached. Key is the {@link TargetedScheduledAction}, value the calculated return value.
	 */
	protected final HashMap<TargetedScheduledAction<?>, CachedValue<?>> componentCache;

	/**
	 * Creates a new {@link ScheduledAgentComponent} that extends {@link AgentComponent} and adds a cache for frequently
	 * calculated values.
	 * 
	 * @param owner
	 */
	public ScheduledAgentComponent(T owner) {
		super(owner);
		this.componentCache = new HashMap<TargetedScheduledAction<?>, CachedValue<?>>();
	}

	/**
	 * Validates if the current value cached in the {@link #componentCache} is valid for a specific
	 * {@link TargetedScheduledAction} at a given {@link Date}.
	 * 
	 * @param method
	 *            the {@link TargetedScheduledAction} the {@link #componentCache} is validated for
	 * @param date
	 *            the {@link Date} the {@link #componentCache} is validated for
	 * @return true if the {@link #componentCache} was valid for the given parameters, false if not
	 */
	protected boolean isCacheValid(TargetedScheduledAction<?> method, Date date) {
		CachedValue<?> cache = null;
		if ((cache = this.componentCache.get(method)) != null)
			return cache.isCacheValid(date);

		return false;
	}

	/**
	 * Manages the {@link ScheduledAgentComponent}'s cache. Tries to update the requested value via a
	 * {@link TargetedScheduledAction} and returns the cached value.
	 * 
	 * @param <R>
	 * @param method
	 *            the {@link TargetedScheduledAction} whose return value should be returned.
	 * @param parameter
	 *            the parameters that are necessary to call method
	 * @return
	 */
	protected <R> CachedResult<R> manageCache(TargetedScheduledAction<R> method, Object... parameter) {
		boolean readFromCache = true;
		try {
			CachedValue<R> cache = (CachedValue<R>) this.componentCache.get(method);
			if (cache == null) {
				cache = new CachedValue<R>();
				this.componentCache.put(method, cache);
			}

			cache.setData(method.scheduledExecute(this.owner.getCurrentDate(), parameter), this.getOwner().getCurrentDate());
			readFromCache = false;
		} catch (ScheduledActionFrequencyException e) {
			// do nothing
		}
		return new CachedResult(readFromCache, (R) this.componentCache.get(method).getData());
	}

	/**
	 * Class to wrapper a value read from a cache, containing information whether the value was read from the cache or
	 * newly generated (because it was out-dated).
	 * 
	 * @author Oliver
	 * 
	 * @param <R>
	 */
	public class CachedResult<R> {
		/**
		 * Indicates whether {@link #requestedPredictions} have been read from cache.
		 */
		private final boolean readFromCache;
		/**
		 * */
		private final R result;

		/**
		 * Creates new {@link CachedResult}
		 * 
		 * @param readFromCache
		 *            see {@link #readFromCache}
		 * @param result
		 *            see {@link #result}
		 */
		protected CachedResult(boolean readFromCache, R result) {
			this.readFromCache = readFromCache;
			this.result = result;
		}

		/**
		 * 
		 * @return {@link #readFromCache}
		 */
		public boolean isReadFromCache() {
			return this.readFromCache;
		}

		/**
		 * 
		 * @return {@link #result}
		 */
		public R getResult() {
			return this.result;
		}
	}
}

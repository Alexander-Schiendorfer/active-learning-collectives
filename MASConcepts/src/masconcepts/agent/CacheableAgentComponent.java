package masconcepts.agent;

import utilities.caching.CachedMap;

public class CacheableAgentComponent<T extends IMessageHandlingAgent> extends AgentComponent {

	/**
	 * A map holding cached values from previous method calls. Keys should be the signature of the method whose value is
	 * cached in the map.
	 */
	protected CachedMap componentCache;

	public CacheableAgentComponent(T owner) {
		super(owner);
		this.componentCache = new CachedMap();
	}
}

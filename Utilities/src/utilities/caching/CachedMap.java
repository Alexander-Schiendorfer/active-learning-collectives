package utilities.caching;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * An {@link ICachedMap} that caches values that are associated with given keys.
 * 
 * @author Gerrit
 * 
 * @param <K>
 *            the type of the key
 * @param <V>
 *            the type of data to be cached
 */
public class CachedMap<K, V> implements ICachedMap<K, V> {

	/**
	 * The cached map.
	 */
	private final HashMap<K, CachedValue<V>> cachedMap = new HashMap<K, CachedValue<V>>();

	@Override
	public Date getCacheDate(K key) {
		CachedValue<V> internalCache = this.cachedMap.get(key);
		if (internalCache != null)
			return internalCache.getCacheDate();

		return null;
	}

	@Override
	public void clearCache(K key) {
		CachedValue<V> internalCache = this.cachedMap.get(key);
		if (internalCache != null)
			internalCache.clearCache();

		this.cachedMap.remove(key);
	}

	@Override
	public V getData(K key) {
		CachedValue<V> internalCache = this.cachedMap.get(key);
		if (internalCache != null)
			return internalCache.getData();

		return null;
	}

	@Override
	public void setData(K key, V value, Date cacheDate) {
		// store value
		CachedValue<V> internalCache = new CachedValue<V>();
		internalCache.setData(value, (Date) cacheDate.clone());
		this.cachedMap.put(key, internalCache);
	}

	@Override
	public boolean isCacheValid(K key, Date date) {
		CachedValue<V> internalCache = this.cachedMap.get(key);
		if (internalCache != null)
			return internalCache.isCacheValid(date);

		return false;
	}

	@Override
	public boolean contains(K key) {
		return this.cachedMap.containsKey(key);
	}

	@Override
	public Set<K> keySet() {
		return this.cachedMap.keySet();
	}

	@Override
	public String toString() {
		return this.cachedMap.toString();
	}
}
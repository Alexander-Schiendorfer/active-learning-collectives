package utilities.caching;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * A cache that supports caching of data that is valid for a specified {@link Date}.
 * 
 * @author Gerrit
 * 
 * @param <K>
 *            the type of the key
 * @param <V>
 *            the type of data to be cached
 */
public interface ICachedMap<K, V> {

	/**
	 * Gets the date for which this cache is valid for the given key.
	 * 
	 * @param key
	 *            identifies the cache date
	 * 
	 * @return
	 */
	public Date getCacheDate(K key);

	/**
	 * Clears the cache, i.e., the cached values are deleted.
	 */
	public void clearCache(K key);

	/**
	 * Gets data that is associated with a given key. <br/>
	 * Returns <code>null</code> in case the data associated with the key equals <code>null</code> or if no data is
	 * available.
	 * 
	 * @param key
	 *            identifies the data to be returned.
	 * @return the data associated with the given key.
	 */
	public V getData(K key);

	/**
	 * Stores given data in the cache and associates the data with the given key. <br/>
	 * The cache date is updated to the given date.<br/>
	 * Existing values associated with the given key are overwritten.
	 * 
	 * @param key
	 *            identifies the data
	 * @param value
	 *            the data to be stored
	 * @param cacheDate
	 *            the date the data is stored
	 */
	public void setData(K key, V value, Date cacheDate);

	/**
	 * Indicates whether this cache is valid for the given date and key.<br/>
	 * The cache is valid if and only if <code>date.equals({@link #getCacheDate()})</code> for the given key. The given
	 * date must not be null.
	 * 
	 * @param key
	 *            identifies the cache date
	 * @param date
	 *            the date used to perform the check
	 * @return <code>true</code> in case the cache is valid for the given key.
	 */
	public boolean isCacheValid(K key, Date date);

	/**
	 * Returns true if the cache contains a mapping for the specified key.
	 * 
	 * @param key
	 *            key whose presence in this cache is to be tested
	 * @return <code>true</code> if this cache contains a mapping for the specified key
	 */
	public boolean contains(K key);

	/**
	 * Returns a Set view of the keys contained in this map. The set is backed by the map, so changes to the map are
	 * reflected in the set, and vice-versa. If the map is modified while an iteration over the set is in progress
	 * (except through the iterator's own remove operation), the results of the iteration are undefined. The set
	 * supports element removal, which removes the corresponding mapping from the map, via the {@link Iterator#remove()}
	 * , {@link Set#remove(Object)}, {@link Set#removeAll(java.util.Collection)},
	 * {@link Set#retainAll(java.util.Collection)}, and {@link Set#clear()} operations. It does not support the
	 * {@link Set#add(Object)} or {@link Set#addAll(java.util.Collection)} operations.
	 * 
	 * @return a set view of the keys contained in this map
	 */
	public Set<K> keySet();
}
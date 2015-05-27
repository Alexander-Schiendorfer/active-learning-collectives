package utilities.caching;

import java.io.Serializable;
import java.util.Date;

/**
 * Caches a single value that is valid for a specified {@link Date}.
 * 
 * @author Gerrit
 * 
 * @param <V>
 *            the type of data to be cached
 */
public class CachedValue<V> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1584992753996697439L;

	/**
	 * The cached value.
	 */
	private V cachedValue = null;

	/**
	 * The date for which {@link #cachedValue} is valid.
	 */
	private Date cacheDate = null;

	/**
	 * The time in ms after which the cache expires.
	 */
	private Long expiresInMs = null;

	/**
	 * Gets the date for which this cache is valid.
	 * 
	 * @return
	 */
	public Date getCacheDate() {
		return this.cacheDate;
	}

	/**
	 * Clears the cache, i.e., the cached value is deleted.
	 */
	public void clearCache() {
		this.cachedValue = null;
		this.cacheDate = null;
		this.expiresInMs = null;
	}

	/**
	 * Gets cached data. <br/>
	 * Returns <code>null</code> if no data is available.
	 * 
	 * @return the data.
	 */
	public V getData() {
		return this.cachedValue;
	}

	/**
	 * Stores given data in the cache. <br/>
	 * The cache date is updated to the given date.<br/>
	 * Existing data is overwritten.<br/>
	 * The cache will only be valid for the given date.
	 * 
	 * @param value
	 *            the data to be stored
	 * @param cacheDate
	 *            the date the data is stored
	 */
	public void setData(V value, Date cacheDate) {
		// set cache date
		this.cacheDate = (Date) cacheDate.clone();
		this.expiresInMs = 1l;
		// store value
		this.cachedValue = value;
	}

	/**
	 * Stores given data in the cache. <br/>
	 * The cache date is updated to the given date.<br/>
	 * Existing data is overwritten.
	 * 
	 * @param value
	 *            the data to be stored
	 * @param cacheDate
	 *            the date the data is stored
	 * @param expiresAt
	 *            the date the cache expires (must not be smaller than cacheDate)
	 */
	public void setData(V value, Date cacheDate, Date expiresAt) {
		if (expiresAt.before(cacheDate))
			throw new IllegalArgumentException("The expiresAt date must not be smaller than the cacheDate!");

		// set cache date
		this.cacheDate = (Date) cacheDate.clone();
		this.expiresInMs = expiresAt.getTime() - cacheDate.getTime();
		// store value
		this.cachedValue = value;
	}

	/**
	 * Indicates whether this cache is valid for the given date.<br/>
	 * The cache is valid iff no more than {@link #expiresInMs} - 1 ms passed (with respect to the given date) since the
	 * cache has been set. If the given date is before {@link #cacheDate}, the cache is not valid.
	 * 
	 * @param date
	 *            the date used to perform the check
	 * @return <code>true</code> in case the cache is valid.
	 */
	public boolean isCacheValid(Date date) {
		if (this.cacheDate == null || date.before(this.cacheDate))
			return false;

		long timeDelta = date.getTime() - this.cacheDate.getTime();

		if (timeDelta >= this.expiresInMs)
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "CachedValue [cachedValue=" + this.cachedValue + ", cacheDate=" + this.cacheDate + "]";
	}
}
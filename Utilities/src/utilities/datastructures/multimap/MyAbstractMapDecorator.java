package utilities.datastructures.multimap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Provides a base decorator that enables additional functionality to be added to a Map via decoration.
 * <p>
 * Methods are forwarded directly to the decorated map.
 * <p>
 * This implementation does not perform any special processing with {@link #entrySet()}, {@link #keySet()} or
 * {@link #values()}. Instead it simply returns the set/collection from the wrapped map. This may be undesirable, for
 * example if you are trying to write a validating implementation it would provide a loophole around the validation.
 * But, you might want that loophole, so this class is kept simple.
 * 
 * @param <K>
 *            the type of the keys in the map
 * @param <V>
 *            the type of the values in the map
 * @since 3.0
 * @version $Id: AbstractMapDecorator.java 1494296 2013-06-18 20:54:29Z tn $
 */
public abstract class MyAbstractMapDecorator<K, V> {

	/** The map to decorate */
	transient Map<K, Collection<V>> map;

	/**
	 * Constructor only used in deserialization, do not use otherwise.
	 * 
	 * @since 3.1
	 */
	protected MyAbstractMapDecorator() {
		super();
	}

	/**
	 * Constructor that wraps (not copies).
	 * 
	 * @param map
	 *            the map to decorate, must not be null
	 * @throws IllegalArgumentException
	 *             if the collection is null
	 */
	protected MyAbstractMapDecorator(final Map<K, Collection<V>> map) {
		if (map == null) {
			throw new IllegalArgumentException("Map must not be null");
		}
		this.map = map;
	}

	/**
	 * Gets the map being decorated.
	 * 
	 * @return the decorated map
	 */
	protected Map<K, Collection<V>> decorated() {
		return this.map;
	}

	// -----------------------------------------------------------------------
	public void clear() {
		this.decorated().clear();
	}

	public boolean containsKey(final K key) {
		return this.decorated().containsKey(key);
	}

	public Set<Map.Entry<K, Collection<V>>> entrySet() {
		return this.decorated().entrySet();
	}

	// FIXME: return concrete type of collection!
	public Collection<V> get(final K key) {
		return this.decorated().get(key);
	}

	public boolean isEmpty() {
		return this.decorated().isEmpty();
	}

	public Set<K> keySet() {
		return this.decorated().keySet();
	}

	public Collection<V> remove(final Object key) {
		return this.decorated().remove(key);
	}

	/**
	 * @return the size in the manner of the number of different keys
	 */
	public int size() {
		return this.decorated().size();
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}
		return this.decorated().equals(object);
	}

	@Override
	public int hashCode() {
		return this.decorated().hashCode();
	}

	@Override
	public String toString() {
		return this.decorated().toString();
	}
}
package utilities.datastructures;

import java.util.Map;
import java.util.TreeMap;

/**
 * A {@link TreeMap}, i.e., a sorted map, that has a maximum size that cannot be exceeded. If a new element is put into
 * the map and the maximum size is exceeded, the first element is removed.
 * 
 * @param <K>
 *            the type of the keys of the {@link TreeMap}
 * @param <V>
 *            the type of the values of the {@link TreeMap}
 */
public class BoundedTreeMap<K, V> extends TreeMap<K, V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5740709925860019832L;

	/**
	 * The maximum size of the {@link BoundedTreeMap}.
	 */
	private final int maximumSize;

	/**
	 * Creates a new {@link BoundedTreeMap} with a maximum size.
	 * 
	 * @param maxSize
	 *            the {@link #maximumSize} of the {@link BoundedTreeMap}. Should at least be 1.
	 */
	public BoundedTreeMap(int maxSize) {
		super();

		if (maxSize < 1)
			throw new IllegalArgumentException("The BoundedTreeMap's maximum size must be >= 1!");

		this.maximumSize = maxSize;
	}

	/**
	 * Puts the given entry into the {@link TreeMap} as specified by {@link TreeMap}. If the maximum size then is
	 * exceeded, the first entry is removed. <br>
	 * Note that this can lead to the case that a given entry is not within the map after calling this method if the
	 * maximum size is exceeded and the entry is smaller than the others.
	 */
	@Override
	public V put(K key, V value) {
		V result = super.put(key, value);
		if (super.size() > this.maximumSize)
			super.remove(super.firstKey());
		return result;
	}

	/**
	 * Repeatedly calls {@link #put(Object, Object)}.
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (K key : map.keySet()) {
			this.put(key, map.get(key));
		}
	}
}
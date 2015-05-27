package utilities.datastructures.multimap;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.FunctorException;
import org.apache.commons.collections4.iterators.IteratorChain;

/**
 * A MultiValueMap2 decorates another map, allowing it to have more than one value for a key.
 * <p>
 * A <code>MultiMap</code> is a Map with slightly different semantics. Putting a value into the map will add the value
 * to a Collection at that key. Getting a value will return a Collection, holding all the values put to that key.
 * <p>
 * This implementation is a decorator, allowing any Map implementation to be used as the base.
 * <p>
 * In addition, this implementation allows the type of collection used for the values to be controlled. By default, an
 * <code>ArrayList</code> is used, however a <code>Class</code> to instantiate may be specified, or a factory that
 * returns a <code>Collection</code> instance.
 * <p>
 * <strong>Note that MultiValueMap2 is not synchronized and is not thread-safe.</strong> If you wish to use this map
 * from multiple threads concurrently, you must use appropriate synchronization. This class may throw exceptions when
 * accessed by concurrent threads without synchronization.
 * 
 * @since 3.2
 * @version $Id: MultiValueMap2.java 1542763 2013-11-17 17:10:33Z tn $
 */
public class MyMultiValueMap<K, V> extends MyAbstractMapDecorator<K, V> implements Serializable, MyMultiMap<K, V> {

	/** Serialization version */
	private static final long serialVersionUID = -2214159910087182007L;

	/** The factory for creating value collections. */
	private final Factory<? extends Collection<V>> collectionFactory;

	/**
	 * Creates a map which wraps the given map and maps keys to ArrayLists.
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param map
	 *            the map to wrap
	 * @return a new multi-value map
	 * @since 4.0
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <K, V> MyMultiValueMap<K, V> createMyMultiValueMap(final Map<K, ? super Collection<V>> map) {
		return MyMultiValueMap.<K, V, ArrayList> createMyMultiValueMap((Map<K, ? super Collection>) map, ArrayList.class);
	}

	/**
	 * Creates a map which decorates the given <code>map</code> and maps keys to collections of type
	 * <code>collectionClass</code>.
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param <C>
	 *            the collection class type
	 * @param map
	 *            the map to wrap
	 * @param collectionClass
	 *            the type of the collection class
	 * @return a new multi-value map
	 * @since 4.0
	 */
	public static <K, V, C extends Collection<V>> MyMultiValueMap<K, V> createMyMultiValueMap(final Map<K, ? super C> map, final Class<C> collectionClass) {
		return new MyMultiValueMap<K, V>(map, new ReflectionFactory<C>(collectionClass));
	}

	/**
	 * Creates a map which decorates the given <code>map</code> and creates the value collections using the supplied
	 * <code>collectionFactory</code>.
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param <C>
	 *            the collection class type
	 * @param map
	 *            the map to decorate
	 * @param collectionFactory
	 *            the collection factory (must return a Collection object).
	 * @return a new multi-value map
	 * @since 4.0
	 */
	public static <K, V, C extends Collection<V>> MyMultiValueMap<K, V> createMyMultiValueMap(final Map<K, ? super C> map, final Factory<C> collectionFactory) {
		return new MyMultiValueMap<K, V>(map, collectionFactory);
	}

	// -----------------------------------------------------------------------
	/**
	 * Creates a MultiValueMap2 based on a <code>HashMap</code> and storing the multiple values in an
	 * <code>ArrayList</code>.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MyMultiValueMap() {
		this(new HashMap<K, Collection<V>>(), new ReflectionFactory(ArrayList.class));
	}

	/**
	 * Creates a MultiValueMap2 which decorates the given <code>map</code> and creates the value collections using the
	 * supplied <code>collectionFactory</code>.
	 * 
	 * @param <C>
	 *            the collection class type
	 * @param map
	 *            the map to decorate
	 * @param collectionFactory
	 *            the collection factory which must return a Collection instance
	 */
	@SuppressWarnings("unchecked")
	protected <C extends Collection<V>> MyMultiValueMap(final Map<K, ? super C> map, final Factory<C> collectionFactory) {
		super((Map<K, Collection<V>>) map);
		if (collectionFactory == null) {
			throw new IllegalArgumentException("The factory must not be null");
		}
		this.collectionFactory = collectionFactory;
	}

	// -----------------------------------------------------------------------
	/**
	 * Write the map out using a custom routine.
	 * 
	 * @param out
	 *            the output stream
	 * @throws IOException
	 * @since 4.0
	 */
	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeObject(this.map);
	}

	/**
	 * Read the map in using a custom routine.
	 * 
	 * @param in
	 *            the input stream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @since 4.0
	 */
	@SuppressWarnings("unchecked")
	// (1) should only fail if input stream is incorrect
	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.map = (Map<K, Collection<V>>) in.readObject(); // (1)
	}

	/**
	 * Removes a specific value from map.
	 * <p>
	 * The item is removed from the collection mapped to the specified key. Other values attached to that key are
	 * unaffected.
	 * <p>
	 * If the last value for a key is removed, <code>null</code> will be returned from a subsequent
	 * <code>get(key)</code>.
	 * 
	 * @param key
	 *            the key to remove from
	 * @param value
	 *            the value to remove
	 * @return {@code true} if the mapping was removed, {@code false} otherwise
	 */
	@Override
	public boolean removeMapping(final K key, final V value) {
		final Collection<V> valuesForKey = this.getCollection(key);
		if (valuesForKey == null) {
			return false;
		}
		final boolean removed = valuesForKey.remove(value);
		if (removed == false) {
			return false;
		}
		if (valuesForKey.isEmpty()) {
			this.remove(key);
		}
		return true;
	}

	/**
	 * Checks whether the map contains the value specified.
	 * <p>
	 * This checks all collections against all keys for the value, and thus could be slow.
	 * 
	 * @param value
	 *            the value to search for
	 * @return true if the map contains the value
	 */
	@Override
	public boolean containsValue(final V value) {
		final Set<Map.Entry<K, Collection<V>>> pairs = this.decorated().entrySet();
		if (pairs != null) {
			for (final Map.Entry<K, Collection<V>> entry : pairs) {
				if (entry.getValue().contains(value)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Adds the value to the collection associated with the specified key.
	 * <p>
	 * Unlike a normal <code>Map</code> the previous value is not replaced. Instead the new value is added to the
	 * collection stored against the key.
	 * 
	 * @param key
	 *            the key to store against
	 * @param value
	 *            the value to add to the collection at the key
	 * @return the value added if the map changed and null if the map did not change
	 */
	@Override
	public V put(final K key, final V value) {
		boolean result = false;

		Collection<V> coll = this.getCollection(key);
		if (coll == null) {
			coll = this.createCollection(1);
			coll.add(value);
			this.decorated().put(key, coll);
		} else {
			coll.add(value);
		}
		result = true; // map definitely changed
		return result ? value : null;
	}

	/**
	 * Gets a collection containing all the values in the map.
	 * <p>
	 * This returns a collection containing the combination of values from all keys.
	 * 
	 * @return a collection view of the values contained in this map
	 */
	@Override
	public Collection<V> values() {
		Collection<V> values = this.createCollection(this.totalSize());
		for (K key : this.decorated().keySet()) {
			values.addAll(this.decorated().get(key));
		}
		return values;
	}

	/**
	 * Checks whether the collection at the specified key contains the value.
	 * 
	 * @param key
	 *            the key to search for
	 * @param value
	 *            the value to search for
	 * @return true if the map contains the value
	 */
	public boolean containsValue(final K key, final V value) {
		final Collection<V> coll = this.getCollection(key);
		if (coll == null) {
			return false;
		}
		return coll.contains(value);
	}

	/**
	 * Gets the collection mapped to the specified key. This method is a convenience method to typecast the result of
	 * <code>get(key)</code>.
	 * 
	 * @param key
	 *            the key to retrieve
	 * @return the collection mapped to the key, null if no mapping
	 */
	public Collection<V> getCollection(final K key) {
		return this.decorated().get(key);
	}

	/**
	 * Gets the size of the collection mapped to the specified key.
	 * 
	 * @param key
	 *            the key to get size for
	 * @return the size of the collection at the key, zero if key not in map
	 */
	public int size(final K key) {
		final Collection<V> coll = this.getCollection(key);
		if (coll == null) {
			return 0;
		}
		return coll.size();
	}

	/**
	 * Adds a collection of values to the collection associated with the specified key.
	 * 
	 * @param key
	 *            the key to store against
	 * @param values
	 *            the values to add to the collection at the key, null ignored
	 * @return true if this map changed
	 */
	public boolean putAll(final K key, final Collection<V> values) {
		if (values == null || values.size() == 0) {
			return false;
		}
		boolean result = false;
		Collection<V> coll = this.getCollection(key);
		if (coll == null) {
			coll = this.createCollection(values.size()); // might produce a non-empty collection
			coll.addAll(values);
			if (coll.size() > 0) {
				// only add if non-zero size to maintain class state
				this.decorated().put(key, coll);
				result = true; // map definitely changed
			}
		} else {
			result = coll.addAll(values);
		}
		return result;
	}

	/**
	 * Gets the total size of the map by counting all the values.
	 * 
	 * @return the total size of the map counting all values
	 */
	public int totalSize() {
		int total = 0;
		for (final Collection<V> v : this.decorated().values()) {
			total += CollectionUtils.size(v);
		}
		return total;
	}

	/**
	 * Creates a new instance of the map value Collection container using the factory.
	 * <p>
	 * This method can be overridden to perform your own processing instead of using the factory.
	 * 
	 * @param size
	 *            the collection size that is about to be added
	 * @return the new collection
	 */
	protected Collection<V> createCollection(final int size) {
		return this.collectionFactory.create();
	}

	// -----------------------------------------------------------------------
	/**
	 * Inner class that provides the values view.
	 */
	private class Values extends AbstractCollection<V> {
		@Override
		public Iterator<V> iterator() {
			final IteratorChain<V> chain = new IteratorChain<V>();
			for (final K k : MyMultiValueMap.this.keySet()) {
				chain.addIterator(new ValuesIterator(k));
			}
			return chain;
		}

		@Override
		public int size() {
			return MyMultiValueMap.this.totalSize();
		}

		@Override
		public void clear() {
			MyMultiValueMap.this.clear();
		}
	}

	/**
	 * Inner class that provides the values iterator.
	 */
	private class ValuesIterator implements Iterator<V> {
		private final K key;
		private final Collection<V> values;
		private final Iterator<V> iterator;

		public ValuesIterator(final K key) {
			this.key = key;
			this.values = MyMultiValueMap.this.getCollection(key);
			this.iterator = this.values.iterator();
		}

		@Override
		public void remove() {
			this.iterator.remove();
			if (this.values.isEmpty()) {
				MyMultiValueMap.this.remove(this.key);
			}
		}

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public V next() {
			return this.iterator.next();
		}
	}

	/**
	 * Inner class that provides a simple reflection factory.
	 */
	private static class ReflectionFactory<T extends Collection<?>> implements Factory<T>, Serializable {

		/** Serialization version */
		private static final long serialVersionUID = 2986114157496788874L;

		private final Class<T> clazz;

		public ReflectionFactory(final Class<T> clazz) {
			this.clazz = clazz;
		}

		@Override
		public T create() {
			try {
				return this.clazz.newInstance();
			} catch (final Exception ex) {
				throw new FunctorException("Cannot instantiate class: " + this.clazz, ex);
			}
		}
	}
}
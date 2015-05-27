/*****************************************************************************
 *
 * Copyright (c) 2014, Patrick Lehner <lehner (dot) patrick (at) gmx (dot) de>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 *****************************************************************************/

package utilities.datastructures;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * <p>
 * An indexed view of a set of elements (of type <code>T</code>). The indexing is permanent, and the set represented by an instance of this class
 * is immutable. This is useful when you need to temporarily or permanently hold additional attributes for every element of a set in some
 * external manner; by having an element-index (and reverse) mapping provided by this class, you can more easily hold such data in indexed lists
 * or arrays, instead of using HashMaps and the like.
 * </p>
 * <p>
 * This is especially advantageous and efficient, if you have algorithms that operate only on those external attributes and do not care about the
 * actual elements most of the time (e.g. Dijkstra's shortest path algorithm cares only about discernible nodes and the edge weights between
 * them, and not about anything else about those nodes). Your algorithm can then work purely index-based improving both memory and time
 * performance as opposed to frequent HashMap or HashSet lookups, and you can translate those indices back to actual elements once the algorithm
 * is done.
 * </p>
 * 
 * <p>
 * The set has a copy constructor ({@link #IndexedSetView(IndexedSetView)} and {@link #IndexedSetView(IndexedSetView, Class)}) for convenience
 * and efficiency.
 * </p>
 * 
 * <p>
 * There are also convenience methods to select a random element (the actual element or its index) from the set, either with a default RNG
 * instance held privately by the set instance, or with an RNG instance provided to the method call (see {@link #getRandomElement()},
 * {@link #getRandomElement(Random)}, {@link #getRandomIndex()} and {@link #getRandomIndex(Random)}).
 * </p>
 * 
 * <p>
 * Created by Patrick Lehner on 2014-10-23.
 * </p>
 * 
 * @param <T>
 *            the type of elements in this set
 * 
 * @author Patrick Lehner
 * @version 2014-11-25
 */
public class IndexedSetView<T> implements Iterable<T> {

	private final Set<T> itemSet;
	private final Map<T, Integer> itemMap;
	private final List<T> itemList;

	private final int size;

	/**
	 * The default random generator used for selecting random elements from this indexed set. It may be any subclass of {@link Random}.
	 */
	private Random rng;

	/**
	 * Create a new indexed set view of the given collection. The collection may include duplicates, which will be trimmed (the collection is
	 * first passed into a Set implementation of the Java standard library to do this, and then converted to a list to derive an index for each
	 * element). Therefore, if you pass an ordered collection into this constructor (e.g. any list), the order of items in this set <b>is likely
	 * to be different</b> from the input list!
	 * 
	 * @param items
	 *            the collection of items to be included in this indexed set
	 */
	public IndexedSetView(final Collection<T> items) {
		this(items, Random.class);
	}

	/**
	 * <p>
	 * Create a new indexed set view of the given collection. The collection may include duplicates, which will be trimmed (the collection is
	 * first passed into a Set implementation of the Java standard library to do this, and then converted to a list to derive an index for each
	 * element). Therefore, if you pass an ordered collection into this constructor (e.g. any list), the order of items in this set <b>is likely
	 * to be different</b> from the input list!
	 * </p>
	 * 
	 * <p>
	 * You can pass the <code>class</code> object of any subclass of {@link Random} as the second parameter, a new instance of which will then be
	 * used as the default random generator for selecting random elements from this set. Note that the class used here must have a default
	 * (parameterless) constructor. If it does not, an appropriate reflection exception is thrown (wrapped in a {@link RuntimeException}, so as
	 * to go undeclared). If you force a class into this parameter that is not a subclass of <code>Random</code>, the construction may work (or
	 * it may fail with some other <code>RuntimeException</code>), but you are then likely to get exceptions once you try to use the random
	 * selection methods.
	 * </p>
	 * 
	 * @param items
	 *            the collection of items to be included in this indexed set
	 * @param randomClass
	 *            the class object for the default random number generator to be used for this class; must have a default (parameterless)
	 *            constructor; default is {@link Random}, which is used automatically with the constructor {@link #IndexedSetView(Collection)}
	 */
	public IndexedSetView(final Collection<T> items, final Class<? extends Random> randomClass) {
		this.setRandomClass(randomClass);

		this.itemSet = Collections.unmodifiableSet(new HashSet<T>(items));
		this.size = itemSet.size();
		this.itemList = Collections.unmodifiableList(new ArrayList<T>(this.itemSet));

		final HashMap<T, Integer> map = new HashMap<T, Integer>(this.size);
		for (int i = 0; i < this.size; ++i)
			map.put(this.itemList.get(i), i);
		this.itemMap = Collections.unmodifiableMap(map);
	}

	/**
	 * <p>
	 * Create a shallow clone of the given indexed set. The internal datastructures pertaining to the indexed set view of the contained item set
	 * are copied and then independent of <b>otherSet</b>. However, the items within <b>otherSet</b> are not cloned themselves. The order of
	 * items in this set will be the same as in <b>otherSet</b>.
	 * </p>
	 * 
	 * <p>
	 * The new set will use a new instance of the same random number generator class that <b>otherSet</b> uses.
	 * </p>
	 * 
	 * @param otherSet
	 *            the original set to be cloned
	 */
	public IndexedSetView(final IndexedSetView<? extends T> otherSet) {
		this(otherSet, otherSet.getRandomClass());
	}

	/**
	 * <p>
	 * Create a shallow clone of the given indexed set. The internal datastructures pertaining to the indexed set view of the contained item set
	 * are copied and then independent of <b>otherSet</b>. However, the items within <b>otherSet</b> are not cloned themselves. The order of
	 * items in this set will be the same as in <b>otherSet</b>.
	 * </p>
	 * 
	 * <p>
	 * You can pass the <code>class</code> object of any subclass of {@link Random} as the second parameter, a new instance of which will then be
	 * used as the default random generator for selecting random elements from this set. Note that the class used here must have a default
	 * (parameterless) constructor. If it does not, an appropriate reflection exception is thrown (wrapped in a {@link RuntimeException}, so as
	 * to go undeclared). If you force a class into this parameter that is not a subclass of <code>Random</code>, the construction may work (or
	 * it may fail with some other <code>RuntimeException</code>), but you are then likely to get exceptions once you try to use the random
	 * selection methods.
	 * </p>
	 * 
	 * @param otherSet
	 *            the original set to be cloned
	 * @param randomClass
	 *            the class object for the default random number generator to be used for this class; must have a default (parameterless)
	 *            constructor; default is the same class as <b>otherSet</b>, which is used automatically with the constructor
	 *            {@link #IndexedSetView(IndexedSetView)}
	 */
	public IndexedSetView(final IndexedSetView<? extends T> otherSet, final Class<? extends Random> randomClass) {
		assert otherSet.itemSet.size() == otherSet.size;
		assert otherSet.itemList.size() == otherSet.size;
		assert otherSet.itemMap.size() == otherSet.size;

		this.setRandomClass(randomClass);

		this.size = otherSet.size;
		this.itemSet = Collections.unmodifiableSet(new HashSet<T>(otherSet.itemSet));
		this.itemList = Collections.unmodifiableList(new ArrayList<T>(otherSet.itemList));
		this.itemMap = Collections.unmodifiableMap(new HashMap<T, Integer>(otherSet.itemMap));
	}

	/**
	 * Get an iterator over the items in this indexed set, in the order of increasing indices
	 * 
	 * @return a new iterator over the items in this indexed set
	 */
	@Override
	public Iterator<T> iterator() {
		return this.itemList.iterator();
	}

	/**
	 * Get an array view of the items in this indexed set, in the proper order. The array is a copy and not directly backed by this set, so
	 * changed to the array (i.e. the references it contains) have no effect on this set (however, changes to the referenced items themselves are
	 * naturally reflected in this set).
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T[] asArray() {
		// have to use explicit cast because we cannot use the alternative signature of toArray(T[]), as you cannot instantiate an array of a
		// type parameter (not even an empty array)
		return (T[]) this.itemList.toArray();
	}

	/**
	 * <p>
	 * Get an unmodifiable list view of the items in this indexed set, in the proper order. Any attempt to modify the returned list results in an
	 * exception (see {@link java.util.Collections#unmodifiableList(java.util.List)}).
	 * </p>
	 * 
	 * <p>
	 * The returned list is a random-access list (it implements {@link java.util.RandomAccess}), i.e. index-based access is possible in constant
	 * time.
	 * </p>
	 * 
	 * @return
	 */
	public List<T> asList() {
		return this.itemList;
	}

	/**
	 * Get an unmodifiable set view of the items in this indexed set. Any attempt to modify the returned set results in an exception (see
	 * {@link java.util.Collections#unmodifiableSet(Set)}).
	 * 
	 * @return
	 */
	public Set<T> asSet() {
		return this.itemSet;
	}

	/**
	 * Get an unmodifiable map which connects each item in this indexed set to its index. Any attempt to modify the returned map results in an
	 * exception (see {@link java.util.Collections#unmodifiableMap(java.util.Map)}).
	 * 
	 * @return
	 */
	public Map<T, Integer> getIndexMap() {
		return this.itemMap;
	}

	/**
	 * Get the index for an element of this indexed set. If the given argument is not an element of this set, -1 is returned.
	 * 
	 * @param t
	 *            the element whose index to retrieve; passing <code>null</code> causes a {@link java.lang.NullPointerException}
	 * @return the index of <code>t</code> in this indexed set, or -1 if <code>t</code> is not a member of this set
	 * @throws java.lang.NullPointerException
	 *             if <code>t</code> is <code>null</code>
	 * 
	 * @see #getIndexEx(Object)
	 */
	public int getIndex(final T t) {
		if (t == null)
			throw new NullPointerException();
		final Integer i = this.itemMap.get(t);
		if (i == null)
			return -1;
		return i;
	}

	/**
	 * Get the index for an element of this indexed set. If the given argument is not an element of this set, a
	 * {@link java.lang.NullPointerException} is thrown.
	 * 
	 * @param t
	 *            the element whose index to retrieve; passing <code>null</code> causes a {@link java.lang.NullPointerException}
	 * @return the index of <code>t</code> in this indexed set (if <code>t</code> is not a member of this set, an exception is thrown
	 * @throws java.lang.NullPointerException
	 *             if <code>t</code> is <code>null</code> or if it is not an element of this set
	 * 
	 * @see #getIndex(Object)
	 */
	public int getIndexEx(final T t) {
		return this.itemMap.get(t);
	}

	/**
	 * Get the element with the given index from this indexed set. Throws an exception if the given index is outside of the bounds of this set.
	 * 
	 * @param index
	 * @return
	 * @throws java.lang.IndexOutOfBoundsException
	 *             if <code>index</code> is out of range (i.e. iff <code>index < 0 || index >= {@link #getSize()}</code>)
	 */
	public T getElement(final int index) {
		return this.itemList.get(index);
	}

	/**
	 * Get the size of this set (the number of elements in it). This is also the size of all structures returned by {@link #asArray()},
	 * {@link #asList()}, {@link #asSet()} and {@link #getIndexMap()}.
	 * 
	 * @return
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * Get a {@link BitSet} with the same size as this indexed set, and with all bits set to {@code false}.
	 * 
	 * @return
	 */
	public BitSet getEmptyBitSet() {
		final BitSet bs = new BitSet(this.size);
		bs.clear(0, this.size - 1);
		return bs;
	}

	/**
	 * Get a {@link BitSet} with the same size as this indexed set, and with all bits set to {@code true}.
	 * 
	 * @return
	 */
	public BitSet getFullBitSet() {
		final BitSet bs = new BitSet(this.size);
		bs.set(0, this.size - 1);
		return bs;
	}

	/**
	 * Get a {@link BitSet} with the same size as this indexed set, with a single bit set to {@code true} (the one with the specified
	 * <b>index</b>), and all other bits set to {@code false}.
	 * 
	 * @param index
	 * @return
	 */
	public BitSet getSingleBitSet(final int index) {
		if (index < 0 || index >= this.size)
			throw new IndexOutOfBoundsException("Index: " + index + "; Size: " + this.size);
		final BitSet bs = this.getEmptyBitSet();
		bs.set(index);
		return bs;
	}

	/**
	 * Get a {@link BitSet} with the same size as this indexed set, with a single bit set to {@code true} (the one corresponding to the specified
	 * <b>element</b>), and all other bits set to {@code false}.
	 * 
	 * @param element
	 * @return
	 */
	public BitSet getSingleBitSet(final T element) {
		return this.getSingleBitSet(this.getIndexEx(element));
	}

	/**
	 * Get the index of a randomly selected element within this set, using the provided random generator to do so.
	 * 
	 * @param rng
	 *            the random number generator to use; you may pass an instance of {@link Random} or any subclass if you need special RNG behavior
	 * @return a randomly selected element within this set
	 * @see #getRandomElement(Random)
	 */
	public int getRandomIndex(Random rng) {
		return rng.nextInt(this.size);
	}

	/**
	 * Get the index of a randomly selected element within this set, using the default random generator of this set instance.
	 * 
	 * @return a randomly selected element within this set
	 * @see #getRandomIndex(Random)
	 * @see #getRandomElement()
	 */
	public int getRandomIndex() {
		return this.getRandomIndex(this.rng);
	}

	/**
	 * Get a randomly selected element from this set, using the provided random generator to select it.
	 * 
	 * @param rng
	 *            the random number generator to use; you may pass an instance of {@link Random} or any subclass if you need special RNG behavior
	 * @return a randomly selected element from this set
	 * @see #getRandomIndex(Random)
	 * @see #getRandomElement()
	 */
	public T getRandomElement(Random rng) {
		return this.itemList.get(this.getRandomIndex(rng));
	}

	/**
	 * Get a randomly selected element from this set, using the default random generator of this set instance.
	 * 
	 * @return a randomly selected element from this set
	 * @see #getRandomIndex()
	 * @see #getRandomElement(Random)
	 */
	public T getRandomElement() {
		return this.itemList.get(this.getRandomIndex());
	}

	/**
	 * Get the runtime class of the default random number generator of this set instance. Access to the actual instance is intentionally
	 * prevented so you cannot modify its internal state.
	 * 
	 * @return a class object identifying the runtime class of the default RNG of this set instance
	 * @see #setRandomClass(Class)
	 */
	public Class<? extends Random> getRandomClass() {
		return this.rng.getClass();
	}

	/**
	 * Set the runtime class of the default random number generator of this set instance, creating a new instance of it right away. This new
	 * instance is used for any following calls to {@link #getRandomIndex()} and {@link #getRandomElement()}. The provided class must have an
	 * accessible default (parameterless) constructor, or an appropriate Reflection exception is thrown (wrapped in a RuntimeException).
	 * 
	 * @param randomClass
	 *            the class of which to create a new instance as default RNG of this set instance
	 * @see #getRandomClass()
	 */
	public void setRandomClass(final Class<? extends Random> randomClass) {
		try {
			this.rng = randomClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Determine if the given object is equal to this indexed set. Indexed sets are considered equal iff they contain exactly the same elements
	 * in exactly the same order. This is the same equality contract as that of {@link List}.
	 * 
	 * @see #hashCode()
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		@SuppressWarnings("rawtypes")
		final IndexedSetView that = (IndexedSetView) o;
		return itemList.equals(that.itemList);
	}

	/**
	 * Get the hashcode for this indexed set view. The hashcode is currently based on the hashcode of the contained list of elements (see
	 * {@link List#hashCode()} and {@link ArrayList#hashCode()}. Therefore, the order of elements has an effect on the resulting hash code.
	 * However, the hashcode for this indexed set is modified so that a list containing the same elements and in the same order as this set will
	 * still have a different hashcode.
	 */
	@Override
	public int hashCode() {
		int result = itemList.hashCode();
		result = 31 * result + size;
		return result;
	}
}

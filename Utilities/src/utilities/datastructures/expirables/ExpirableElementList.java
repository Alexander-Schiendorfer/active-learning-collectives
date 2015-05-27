package utilities.datastructures.expirables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import utilities.datastructures.RingBuffer;

/**
 * Class representing a list of {@link IExpirable}s that are automatically deleted as soon as such an element is
 * expired.
 *
 * @author Gerrit
 *
 * @param <T>
 *            the information used to check whether an {@link IExpirable} is expired.
 * @param <E>
 *            the concrete type of the elements in the list
 */
public class ExpirableElementList<T, E extends IExpirable<T>> {

	/**
	 * Used to hold the list's elements.
	 */
	private final LinkedList<E> elements;

	/**
	 * Creates a new instance of {@link ExpirableElementList}.
	 */
	public ExpirableElementList() {
		this.elements = new LinkedList<E>();
	}

	/**
	 * Inserts a new element to the end of the list.
	 *
	 * @param newElement
	 *            the element to be added to the list.
	 */
	public void add(E newElement) {
		// put the new element into the buffer
		this.elements.add(newElement);
	}

	/**
	 * Returns the buffer's elements in the form of a {@link LinkedList}.
	 *
	 * @return
	 */
	public ArrayList<E> getElementsCopy() {
		return new ArrayList<E>(this.elements);
	}

	/**
	 * Gets all elements for which {@link IExpirable#isExpired(Object)} does <b>not</b> evaluate to <code>true</code>.
	 *
	 * @param checkForExpiredInfo
	 *            information used to check whether an element of the list is expired.
	 * @return
	 */
	public LinkedList<E> getNonExpiredElements(T checkForExpiredInfo) {
		Iterator<E> elementIterator = this.iterator(checkForExpiredInfo);

		LinkedList<E> relevantNonExpiredElements = new LinkedList<E>();
		while (elementIterator.hasNext()) {
			relevantNonExpiredElements.add(elementIterator.next());
		}

		return relevantNonExpiredElements;
	}

	/**
	 * Returns the {@link RingBuffer}'s current size.
	 *
	 * @return
	 */
	public int size() {
		return this.elements.size();
	}

	/**
	 * Indicates whether this {@link RingBuffer} is empty.
	 *
	 * @return
	 */
	public boolean isEmpty() {
		return this.elements.isEmpty();
	}

	/**
	 * Gets the last element in this {@link RingBuffer}.
	 *
	 * @return
	 */
	public E getLast() {
		return this.elements.getLast();
	}

	/**
	 * Gets the first element in this {@link RingBuffer}.
	 *
	 * @return
	 */
	public E getFirst() {
		return this.elements.getFirst();
	}

	/**
	 * Removes all of the elements from the {@link RingBuffer}.
	 */
	public void clear() {
		this.elements.clear();
	}

	/**
	 * Gets an {@link Iterator} for the list of elements.<br/>
	 * <br/>
	 * When calling {@link Iterator#hasNext()} or {@link Iterator#next()}, the iterator only regards those elements for
	 * which {@link IExpirable#isExpired(Object)} does <b>not</b> evaluate to <code>true</code>. <br/>
	 * <br/>
	 * Those elements for which {@link IExpirable#isExpired(Object)} <b>does</b> evaluate to <code>true</code> are
	 * removed from the list on-the-fly.
	 *
	 * @param checkForExpiredInfo
	 *            information used to check whether an element of the list is expired.
	 * @return
	 */
	public Iterator<E> iterator(final T checkForExpiredInfo) {
		return new Iterator<E>() {

			/**
			 * The current index with regard to {@link ExpirableElementList#elements} (note that the element list might
			 * change during the use of this iterator).
			 */
			private int elementsIndex = 0;

			/**
			 * A copy of {@link ExpirableElementList#elements} that does not change during the use of this iterator.
			 */
			private ArrayList<E> elementsCopy = ExpirableElementList.this.getElementsCopy();

			/**
			 * The current index with regard to {@link #elementsCopyIndex}.
			 */
			private int elementsCopyIndex = 0;

			@Override
			public boolean hasNext() {
				int sizeElementsCopy = this.elementsCopy.size();
				while (this.elementsCopyIndex < sizeElementsCopy) {
					if (this.elementsCopy.get(this.elementsCopyIndex).isExpired(checkForExpiredInfo)) {
						// remove expired elements on-the-fly
						ExpirableElementList.this.elements.remove(this.elementsIndex);
						this.elementsCopyIndex++;
					} else {
						// non-expired element found
						return true;
					}
				}

				// no non-expired elements available
				return false;
			}

			@Override
			public E next() {
				if (this.hasNext()) {
					E nextElement = this.elementsCopy.get(this.elementsCopyIndex++);
					this.elementsIndex++;
					return nextElement;
				} else
					throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not implemented!");
			}
		};
	}

	@Override
	public String toString() {
		return this.elements.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.elements == null) ? 0 : this.elements.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		ExpirableElementList other = (ExpirableElementList) obj;
		if (this.elements == null) {
			if (other.elements != null)
				return false;
		} else if (!this.elements.equals(other.elements))
			return false;
		return true;
	}
}

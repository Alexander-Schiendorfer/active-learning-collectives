package utilities.datastructures;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Specifies a buffer with a defined maximum size. New elements are inserted at the end of the buffer. If the maximum
 * size of the buffer is exceeded, the first element is removed.
 *
 * @author Gerrit
 *
 * @param <E>
 *            The type of element this {@link RingBuffer} manages.
 */
public class RingBuffer<E> implements Serializable, Iterable<E> {

	/**
	 * Used for serialization purposes.
	 */
	private static final long serialVersionUID = 6462761580761764540L;

	/**
	 * This {@link RingBuffer}'s maximum size.
	 */
	private final int maxSize;

	/**
	 * Used to hold the buffer's elements.
	 */
	protected final LinkedList<E> elements;

	/**
	 * Creates a new instance of {@link RingBuffer} with the maximum size of <code>maxSize</code>.
	 *
	 * @param maxSize
	 */
	public RingBuffer(int maxSize) {
		if (maxSize < 0)
			throw new IllegalArgumentException("The buffer's maximum size must be >=0 !");

		this.maxSize = maxSize;
		this.elements = new LinkedList<E>();
	}

	/**
	 * Copies the given {@link RingBuffer}
	 *
	 * @param copy_this
	 */
	public RingBuffer(RingBuffer<E> copy_this) {
		this.maxSize = copy_this.maxSize;
		this.elements = new LinkedList<E>(copy_this.elements);
	}

	/**
	 * Inserts a new element to the end of the buffer. <br/>
	 * If the buffer exceeds its {@link #maxSize}, the first element is removed.
	 *
	 * @param newElement
	 *            the element to be added to the buffer.
	 */
	public void add(E newElement) {
		if (this.maxSize != 0) {
			// put the new element into the buffer
			this.elements.add(newElement);

			// If the buffer exceeds its maximum size, remove the first element.
			if (this.elements.size() > this.maxSize) {
				this.elements.removeFirst();
			}
		}
	}

	/**
	 * Returns the buffer's elements in the form of a {@link LinkedList}.
	 *
	 * @return
	 */
	public LinkedList<E> getElementsCopy() {
		return new LinkedList<E>(this.elements);
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

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {

			private int cursor = 0;
			private int length = RingBuffer.this.elements.size();

			@Override
			public boolean hasNext() {
				return this.cursor < this.length;
			}

			@Override
			public E next() {
				if (this.hasNext()) {
					E element = RingBuffer.this.elements.get(this.cursor++);
					return element;
				} else
					throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public String toString() {
		return "Maximum size: " + this.maxSize + "; elements: " + this.elements.toString();
	}
}
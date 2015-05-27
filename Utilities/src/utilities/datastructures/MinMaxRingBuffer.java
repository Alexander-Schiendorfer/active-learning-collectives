package utilities.datastructures;

import java.util.Collections;

public class MinMaxRingBuffer<E extends Comparable<E>> extends RingBuffer<E> {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = 2493104377663128256L;

	/**
	 * The all-time minimum value contained in this {@link RingBuffer}.
	 */
	private E allTimeMax;

	/**
	 * The all-time maximum value contained in this {@link RingBuffer}.
	 */
	private E allTimeMin;

	/**
	 * Creates a new {@link MinMaxRingBuffer}
	 *
	 * @param maxSize
	 *            the maximum size of the buffer
	 * @param min
	 *            the lower bound for elements added to the buffer
	 * @param max
	 *            the upper bound for elements added to the buffer
	 */
	public MinMaxRingBuffer(int maxSize, E min, E max) {
		super(maxSize);

		// Initialize this.max with min and this.min with max to guarantee that the first call of add(newElement)
		// overwrites this.min and this.max.
		this.allTimeMax = min;
		this.allTimeMin = max;
	}

	@Override
	public void add(E newElement) {
		super.add(newElement);

		// update min and max values
		if (this.allTimeMax != null)
			this.allTimeMax = this.allTimeMax.compareTo(newElement) == 1 ? this.allTimeMax : newElement;
		else
			this.allTimeMax = newElement;

		if (this.allTimeMin != null)
			this.allTimeMin = this.allTimeMin.compareTo(newElement) != 1 ? this.allTimeMin : newElement;
		else
			this.allTimeMin = newElement;
	}

	/**
	 * Returns the all-time {@link #allTimeMax}.
	 *
	 * @return
	 */
	public E getAllTimeMax() {
		return this.allTimeMax;
	}

	/**
	 * Returns the all-time {@link #allTimeMin}.
	 *
	 * @return
	 */
	public E getAllTimeMin() {
		return this.allTimeMin;
	}

	/**
	 *
	 * @return the current max. value contained in this {@link MinMaxRingBuffer}.
	 */
	public E calculateContainedMax() {
		return Collections.max(this.elements);
	}

	/**
	 *
	 * @return the current min. value contained in this {@link MinMaxRingBuffer}.
	 */
	public E calculateContainedMin() {
		return Collections.min(this.elements);
	}
}

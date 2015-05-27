package utilities.datastructures;

import java.util.Arrays;

public class BoundedDoubleArray {

	/**
	 * The underlying array.
	 */
	private final double[] array;

	/**
	 * The size of the array.
	 */
	private int size;

	/**
	 * Creates a new {@link BoundedDoubleArray} with length <code>maxSize</code>.
	 *
	 * @param maxSize
	 */
	public BoundedDoubleArray(int maxSize) {
		this.array = new double[maxSize];
	}

	/**
	 * Sets the {@link #size} of the {@link BoundedDoubleArray} and checks whether the underlying bounded array has at
	 * least the given size. If the given size exceeds the size of the underlying array, a {@link RuntimeException} is
	 * thrown.
	 *
	 * @param size
	 */
	public void ensureSize(int size) {
		if (size > this.array.length) {
			throw new RuntimeException("The bounded double array only has size " + this.array.length + ", the desired size is " + size);
		}

		this.size = size;
	}

	/**
	 *
	 * @return {@link #array}
	 */
	public double[] getArray() {
		return this.array;
	}

	/**
	 *
	 * @return {@link #size}
	 */
	public int getSize() {
		return this.size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.array);
		result = prime * result + this.size;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		BoundedDoubleArray other = (BoundedDoubleArray) obj;
		if (!Arrays.equals(this.array, other.array)) {
			return false;
		}
		if (this.size != other.size) {
			return false;
		}
		return true;
	}
}
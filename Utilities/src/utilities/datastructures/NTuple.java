package utilities.datastructures;

import java.util.Arrays;

/**
 * Class that represents an n-tuple of objects.
 */
public class NTuple {

	/**
	 * The array of {@link Object}s that is held by this {@link NTuple}.
	 */
	private final Object[] tupleObjects;

	/**
	 * Creates a new {@link NTuple} with the given objects and the length of the number of the given objects.
	 * 
	 * @param tupleObjects
	 */
	public NTuple(Object... tupleObjects) {
		super();
		this.tupleObjects = tupleObjects;
	}

	/**
	 * @return the tuple objects
	 */
	public Object[] getTupleObjects() {
		return this.tupleObjects;
	}

	/**
	 * @return the size of this {@link NTuple}
	 */
	public int size() {
		return this.tupleObjects.length;
	}

	/**
	 * Returns the object of the {@link NTuple} at the given index.
	 * 
	 * @param index
	 *            the index of the object to get
	 * @return the object of the {@link NTuple} at the given index
	 * @throws IndexOutOfBoundsException
	 *             if the index is not within the bounds of the {@link NTuple}
	 */
	public Object get(int index) {
		if (index < 0 || index > this.tupleObjects.length - 1) {
			throw new IndexOutOfBoundsException("Index must be between 0 and " + (this.tupleObjects.length - 1) + "! Given index: " + index);
		}
		return this.tupleObjects[index];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.tupleObjects);
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
		NTuple other = (NTuple) obj;
		if (!Arrays.equals(this.tupleObjects, other.tupleObjects))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NTuple " + Arrays.toString(this.tupleObjects);
	}
}
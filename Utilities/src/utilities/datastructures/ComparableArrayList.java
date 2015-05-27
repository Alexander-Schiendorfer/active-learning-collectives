package utilities.datastructures;

import java.util.ArrayList;

/**
 * An {@link ArrayList} that implements {@link Comparable}. Two {@link ComparableArrayList}s are compared with regard to
 * their elements and length.
 * 
 * @author Gerrit
 * 
 * @param <E>
 *            the type of the elements
 */
public class ComparableArrayList<E extends Comparable<E>> extends ArrayList<E> implements Comparable<ComparableArrayList<E>> {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = -8221735950534381469L;

	/**
	 * Constructs an empty list with the specified initial capacity.
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the list
	 * @throws IllegalArgumentException
	 *             if the specified initial capacity is negative
	 */
	public ComparableArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	public int compareTo(ComparableArrayList<E> o) {
		if (this.equals(o))
			return 0;

		int oSize = o.size();
		int index = 0;
		for (E elem : this) {
			if (oSize > index) {
				int compareRes = elem.compareTo(o.get(index));

				// if the result of the comparison is not equal, this result should be returned
				if (compareRes != 0)
					return compareRes;
			}
			// o is shorter than this
			else {
				return -1;
			}

			index++;
		}

		// o is longer than this
		if (oSize > this.size())
			return 1;

		// o and this are of the same size and compare to of corresponding elements yields 0
		throw new RuntimeException("The comparable lists are not equal but the compare function of the elements yields always 0!");
	}

	@Override
	public String toString() {
		return super.toString();
	}
}

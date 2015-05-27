package utilities.datastructures;

import java.io.Serializable;

/**
 * Class that represents a 2-tuple. <br>
 * <br>
 * 
 * Note that only {@link TwoTuple}s with the same order of types and pairwise equal elements are considered equal;
 * {@link TwoTuple}s with a different order of types are not considered equal (even though their elements might be
 * equal).
 * 
 * @param <S>
 *            the concrete type of the first element
 * @param <T>
 *            the concrete type of the second element
 */
public class TwoTuple<S, T> implements Serializable {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = -6928975049863911377L;

	/**
	 * The first element.
	 */
	private S first;

	/**
	 * The second element.
	 */
	private T second;

	/**
	 * Creates a new {@link Triple}.
	 * 
	 * @param first
	 *            the first element
	 * @param second
	 *            the second element
	 */
	public TwoTuple(S first, T second) {
		super();
		this.first = first;
		this.second = second;
	}

	/**
	 * @return the first element
	 */
	public S getFirst() {
		return this.first;
	}

	/**
	 * @return the second element
	 */
	public T getSecond() {
		return this.second;
	}

	/**
	 * Sets {@link #first}.
	 * 
	 * @param first
	 */
	public void setFirst(S first) {
		this.first = first;
	}

	/**
	 * Sets {@link #second}.
	 * 
	 * @param second
	 */
	public void setSecond(T second) {
		this.second = second;
	}

	@Override
	public String toString() {
		return "TwoTuple [first=" + this.first + ", second=" + this.second + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.first == null) ? 0 : this.first.hashCode());
		result = prime * result + ((this.second == null) ? 0 : this.second.hashCode());
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
		TwoTuple<?, ?> other = (TwoTuple<?, ?>) obj;
		if (this.first == null) {
			if (other.first != null)
				return false;
		} else if (!this.first.equals(other.first))
			return false;
		if (this.second == null) {
			if (other.second != null)
				return false;
		} else if (!this.second.equals(other.second))
			return false;
		return true;
	}
}
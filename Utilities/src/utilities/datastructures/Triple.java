package utilities.datastructures;

import java.io.Serializable;

/**
 * Class that represents a triple. <br>
 * <br>
 * 
 * Note that only {@link Triple}s with the same order of types and pairwise equal elements are considered equal;
 * {@link Triple}s with a different order of types are not considered equal (even though their elements might be equal).
 * 
 * @param <S>
 *            the concrete type of the first element
 * @param <T>
 *            the concrete type of the second element
 * @param <U>
 *            the concrete type of the third element
 */
public class Triple<S, T, U> implements Serializable {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = -1972678124659344343L;

	/**
	 * The first element.
	 */
	private S first;

	/**
	 * The second element.
	 */
	private T second;

	/**
	 * The third element.
	 */
	private U third;

	/**
	 * Creates a new {@link Triple}.
	 * 
	 * @param first
	 *            the first element
	 * @param second
	 *            the second element
	 * @param third
	 *            the third element
	 */
	public Triple(S first, T second, U third) {
		super();
		this.first = first;
		this.second = second;
		this.third = third;
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
	 * @return the third element
	 */
	public U getThird() {
		return this.third;
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

	/**
	 * Sets {@link #third}.
	 * 
	 * @param third
	 */
	public void setThird(U third) {
		this.third = third;
	}

	@Override
	public String toString() {
		return "Triple [first=" + this.first + ", second=" + this.second + ", third=" + this.third + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.first == null) ? 0 : this.first.hashCode());
		result = prime * result + ((this.second == null) ? 0 : this.second.hashCode());
		result = prime * result + ((this.third == null) ? 0 : this.third.hashCode());
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
		Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
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
		if (this.third == null) {
			if (other.third != null)
				return false;
		} else if (!this.third.equals(other.third))
			return false;
		return true;
	}
}
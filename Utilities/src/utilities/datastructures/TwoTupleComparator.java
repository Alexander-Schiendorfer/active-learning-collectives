package utilities.datastructures;

import java.util.Comparator;

/**
 * An implementation of {@link Comparator} that lexicographically compares {@link TwoTuple}s containing
 * {@link Comparable}s.
 *
 * @param <S>
 *            the type of the {@link TwoTuple}'s first element
 * @param <T>
 *            the type of the {@link TwoTuple}s second element
 */
public class TwoTupleComparator<S extends Comparable<S>, T extends Comparable<T>> implements Comparator<TwoTuple<S, T>> {

	@Override
	public int compare(TwoTuple<S, T> o1, TwoTuple<S, T> o2) {
		int result = 0;
		result = o1.getFirst().compareTo(o2.getFirst());
		if (result == 0) {
			result = o1.getSecond().compareTo(o2.getSecond());
		}
		return result;
	}
}
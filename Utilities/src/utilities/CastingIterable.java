package utilities;

import java.util.Iterator;

public class CastingIterable<T> implements Iterable<T> {

	private final Iterable<?> baseIterable;

	public CastingIterable(final Iterable<?> baseIterable) {
		this.baseIterable = baseIterable;
	}

	@Override
	public Iterator<T> iterator() {
		return new CastingIterator<T>(this.baseIterable.iterator());
	}

}

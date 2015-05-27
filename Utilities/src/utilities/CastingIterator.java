package utilities;

import java.util.Iterator;

public class CastingIterator<T> implements Iterator<T> {
	private final Iterator<?> baseIterator;

	public CastingIterator(final Iterator<?> baseIterator) {
		this.baseIterator = baseIterator;
	}

	@Override
	public boolean hasNext() {
		return this.baseIterator.hasNext();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		return (T) this.baseIterator.next();
	}

	@Override
	public void remove() {
		this.baseIterator.remove();
	}

}
package optimizationtools.util;

import java.util.Iterator;

import optimizationtools.datawriter.ICPLEXElement;

public class PrettyPassthroughIterator implements Iterator<String> {
	private final int startIndent;
	private final int indentBy;
	private final boolean prettyPrintingEnabled;
	private Iterator<?> it;

	public PrettyPassthroughIterator(Iterator<?> it, final int startIndent, final int indentBy, final boolean prettyPrintingEnabled) {
		this.it = it;
		this.startIndent = startIndent;
		this.indentBy = indentBy;
		this.prettyPrintingEnabled = prettyPrintingEnabled;
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public String next() {
		final Object o = it.next();
		if (o instanceof ICPLEXElement)
			return ((ICPLEXElement) o).getContent(prettyPrintingEnabled, startIndent + indentBy, indentBy);
		else
			return o.toString();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("removing items via the iterator is not supported.");
	}

}
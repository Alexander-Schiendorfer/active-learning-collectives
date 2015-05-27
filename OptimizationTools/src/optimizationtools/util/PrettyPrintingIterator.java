package optimizationtools.util;

import java.util.Iterator;

import optimizationtools.datawriter.ICPLEXElement;
import optimizationtools.datawriter.ICPLEXPrettyPrintable;

public class PrettyPrintingIterator implements Iterator<String> {
	private final int startIndent;
	private final int indentBy;
	private final boolean prettyPrintingEnabled;
	private Iterator<ICPLEXElement> it;
	private final String nameValJoinString;
	private final String indentStr;

	public PrettyPrintingIterator(Iterator<ICPLEXElement> it, final int startIndent, final int indentBy, final boolean prettyPrintingEnabled,
			final String nameValJoinString) {
		this.it = it;
		this.startIndent = startIndent;
		this.indentBy = indentBy;
		this.prettyPrintingEnabled = prettyPrintingEnabled;
		this.nameValJoinString = nameValJoinString;
		indentStr = StringUtils.repeatChar(startIndent, ' ');
	}

	public PrettyPrintingIterator(Iterator<ICPLEXElement> it, final int startIndent, final int indentBy, final boolean prettyPrintingEnabled) {
		this(it, startIndent, indentBy, prettyPrintingEnabled, " : ");
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public String next() {
		final ICPLEXElement e = it.next();
		StringBuilder sb = new StringBuilder();
		if (e.getName() != null && !e.getName().isEmpty())
			sb.append(e.getName()).append(nameValJoinString);
		if (e instanceof ICPLEXPrettyPrintable)
			sb.append('\n').append(indentStr);
		sb.append(e.getContent(prettyPrintingEnabled, startIndent + indentBy, indentBy));
		return sb.toString();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("removing items via the iterator is not supported.");
	}

}
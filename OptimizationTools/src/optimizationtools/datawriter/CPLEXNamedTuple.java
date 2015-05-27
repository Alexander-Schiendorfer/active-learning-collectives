package optimizationtools.datawriter;

import java.util.LinkedList;
import java.util.List;

import optimizationtools.util.PrettyPrintingIterator;
import optimizationtools.util.StringUtils;

public class CPLEXNamedTuple extends CPLEXAbstractElement implements ICPLEXPrettyPrintable {

	protected List<ICPLEXElement> items;

	public CPLEXNamedTuple(String name, List<ICPLEXElement> items) {
		this.items = items;
		this.name = name;
	}

	public CPLEXNamedTuple(String name) {
		this(name, new LinkedList<ICPLEXElement>());
	}

	@Override
	public String getContent(boolean prettyPrintingEnabled, final int startIndent, final int indentBy) {
		final String indentStr = StringUtils.repeatChar(startIndent, ' ');
		String prefix = "#< ";
		if (indentBy > 3)
			prefix += StringUtils.repeatChar(indentBy - 3, ' ');
		return StringUtils.implodeIterable(new PrettyPrintingIterator(items.iterator(), startIndent, indentBy, prettyPrintingEnabled), prefix, ">#", ",\n"
				+ indentStr, " ");
	}

	public List<ICPLEXElement> getItems() {
		return items;
	}
}

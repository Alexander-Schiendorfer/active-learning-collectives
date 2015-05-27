package optimizationtools.datawriter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import optimizationtools.util.PrettyPrintingIterator;
import optimizationtools.util.StringUtils;

/**
 * <p>
 * An array which uses explicit indices for its elements, e.g. when the index type is a set or other unordered type.
 * </p>
 * 
 * <p>
 * The index used for each element is that element's name (which is one reason why the items of this array must be
 * {@link ICPLEXElement}s.
 * </p>
 * 
 * <p>
 * The values will be printed in the order they are returned by the iterator of the {@code items} list.
 * </p>
 * 
 * @author lehnepat
 * 
 */
public class CPLEXIndexedArray extends CPLEXAbstractElement implements ICPLEXPrettyPrintable {

	protected List<ICPLEXElement> items;

	/**
	 * Create a new {@code CPLEXIndexedArray} instance with the given <b>name</b> and a new, empty {@link LinkedList} of
	 * elements.
	 * 
	 * @param name
	 */
	public CPLEXIndexedArray(String name) {
		this(name, new ArrayList<ICPLEXElement>());
	}

	/**
	 * Create a new {@code CPLEXIndexedArray} instance with the given <b>name</b> and the given list of <b>items</b>.
	 * 
	 * @param name
	 * @param items
	 */
	public CPLEXIndexedArray(String name, List<ICPLEXElement> items) {
		this.items = items;
		this.name = name;
	}

	public List<ICPLEXElement> getItems() {
		return items;
	}

	@Override
	public String getContent(boolean prettyPrintingEnabled, final int startIndent, final int indentBy) {
		final String indentStr = StringUtils.repeatChar(startIndent, ' ');
		return StringUtils.implodeIterable(new PrettyPrintingIterator(items.iterator(), startIndent, indentBy, prettyPrintingEnabled), "#[\n" + indentStr,
				"]#", ",\n" + indentStr, " ");
	}
}

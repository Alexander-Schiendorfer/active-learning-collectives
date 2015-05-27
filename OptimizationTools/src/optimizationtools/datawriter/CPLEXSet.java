package optimizationtools.datawriter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import optimizationtools.util.PrettyPassthroughIterator;
import optimizationtools.util.StringUtils;

/**
 * A set of elements (i.e. the elements are generally unordnered and each element may appear at most once in the set).
 * 
 * <p>
 * The values will be printed in the order they are returned by the iterator of the {@code items} set.
 * </p>
 * 
 * @author lehnepat
 * 
 * @param <T>
 *            the type of the elements contained in this set
 */
public class CPLEXSet<T> extends CPLEXAbstractElement {

	protected Set<T> items;

	/**
	 * Create a new {@code CPLEXSet} instance with the given <b>name</b> and set of <b>items</b>.
	 * 
	 * @param name
	 * @param items
	 */
	public CPLEXSet(String name, Set<T> items) {
		this.items = items;
		this.name = name;
	}

	/**
	 * Create a new {@code CPLEXSet} instance with the given <b>name</b> and a new, empty {@link HashSet} of items.
	 * 
	 * @param name
	 */
	public CPLEXSet(String name) {
		this(name, new HashSet<T>());
	}

	/**
	 * Create a new {@code CPLEXSet} instance with the given <b>name</b> and a new instance of {@link HashSet}
	 * containing all elements of <b>items</b> (duplicates trimmed).
	 * 
	 * @param name
	 * @param values
	 */
	public CPLEXSet(String name, Collection<T> values) {
		this(name, new HashSet<T>(values));
	}

	public Set<T> getItems() {
		return items;
	}

	@Override
	public String getContent(boolean prettyPrintingEnabled, final int startIndent, final int indentBy) {
		if (items == null)
			return "{ }";
		else
			return StringUtils.implodeIterable(new PrettyPassthroughIterator(items.iterator(), startIndent, indentBy, prettyPrintingEnabled), "{ ", "}", ", ",
					" ");
	}
}

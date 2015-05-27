package optimizationtools.datawriter;

import java.util.LinkedList;
import java.util.List;

import optimizationtools.util.PrettyPassthroughIterator;
import optimizationtools.util.StringUtils;

/**
 * <p>
 * An array which does <b>not</b> use explicit indices for its elements, used to define the content of arrays whose
 * index runs over a range, for example.
 * </p>
 * 
 * <p>
 * The values will be printed in the order they are returned by the iterator of the {@code items} list.
 * </p>
 * 
 * <p>
 * The values are formatted by using their {@code toString()} function.
 * </p>
 * 
 * @author lehnepat
 * 
 * @param <T>
 *            the type of this array's elements.
 */
public class CPLEXArray<T> extends CPLEXAbstractElement {

	protected List<T> items;

	/**
	 * Create a new {@code CPLEXArray} instance of the given <b>name</b> and with the given list of <b>items</b>.
	 * 
	 * @param name
	 * @param items
	 */
	public CPLEXArray(String name, List<T> items) {
		this.items = items;
		this.name = name;
	}

	/**
	 * Create a new {@code CPLEXArray} instance of the given <b>name</b> and a new, empty {@link LinkedList} of
	 * elements.
	 * 
	 * @param name
	 */
	public CPLEXArray(String name) {
		this(name, new LinkedList<T>());
	}

	public List<T> getItems() {
		return items;
	}

	@Override
	public String getContent(boolean prettyPrintingEnabled, final int startIndent, final int indentBy) {
		return StringUtils.implodeIterable(new PrettyPassthroughIterator(items.iterator(), startIndent, indentBy, prettyPrintingEnabled), "[ ", "]", ",", " ");
	};

	public static CPLEXArray<CPLEXFloatConstant> createDoubleArray(String name, List<Double> array) {
		LinkedList<CPLEXFloatConstant> resulList = new LinkedList<CPLEXFloatConstant>();

		for (double d : array) {
			resulList.add(new CPLEXFloatConstant(null, d));
		}
		return new CPLEXArray<CPLEXFloatConstant>(name, resulList);
	}
}

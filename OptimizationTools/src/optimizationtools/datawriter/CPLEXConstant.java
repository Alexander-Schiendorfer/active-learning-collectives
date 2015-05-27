package optimizationtools.datawriter;

/**
 * A wrapper class for any object that is not any of the other types of CPLEX elements (array, set or tuple). It uses
 * the wrapped object's {@link Object#toString() toString()} method to format it into the data file output, so make sure
 * that that method is adequately implemented.
 * 
 * @author lehnepat
 * 
 * @param <T>
 *            the type of the object wrapped in this element
 */
public class CPLEXConstant<T> extends CPLEXAbstractElement {
	protected final T item;

	public CPLEXConstant(final String name, final T item) {
		this.name = name;
		this.item = item;
	}

	public T getItem() {
		return item;
	}

	@Override
	public String getContent(boolean prettyPrintingEnabled, int startIndent, int indentBy) {
		return item.toString();
	}
}

package optimizationtools.datawriter;

/**
 * Maps an element of type <b>{@literal <T>}</b> to a {@code CPLEXElement}. This is useful for types which do not have a
 * trivial mapping, whose {@code toString()} cannot be used for formatting the element or for cases where you do not
 * want the processing/formatting code in the original class.
 * 
 * @author lehnepat
 * 
 * @see CPLEXDataWriter#createIndexedArray(String, java.util.Map, CPLEXElementMapper)
 * 
 * @param <T>
 *            the type of the object to be mapped to an element
 * @param <E>
 *            the type of element to which the given object is mapped
 */
public interface ICPLEXElementMapper<T, E extends ICPLEXElement> {
	/**
	 * Get a {@link ICPLEXElement} representing <b>obj</b>.
	 * 
	 * @see CPLEXDataWriter#createIndexedArray(String, java.util.Map, CPLEXElementMapper)
	 * 
	 * @param obj
	 *            the object to mapped to a {@code CPLEXElement}
	 * @return a {@link ICPLEXElement} representing <b>obj</b>
	 */
	public E getElement(T obj);
}

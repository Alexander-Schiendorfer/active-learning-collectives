package optimizationtools.datawriter;

import java.util.List;

/**
 * Maps an element of type <b>{@literal <T>}</b> to a key-value map of attributes (in the form of a list of
 * {@link ICPLEXElement}s), which can be wrapped in a {@link CPLEXNamedTuple}.
 * 
 * @author lehnepat
 * 
 * @see CPLEXDataWriter#createTupleArray(String, java.util.Map, CPLEXTupleMapper)
 * 
 * @param <T>
 */
public interface ICPLEXTupleMapper<T> {
	/**
	 * Get a list of {@link ICPLEXElement}s representing the attributes of <b>obj</b>.
	 * 
	 * @see CPLEXDataWriter#createTupleArray(String, java.util.Map, CPLEXTupleMapper)
	 * 
	 * @param obj
	 *            the object to mapped to a {@code CPLEXElement} list
	 * @return a list of {@link ICPLEXElement}s representing the attributes of <b>obj</b>
	 */
	public List<ICPLEXElement> getValueList(T obj);
}

package utilities.datastructures;

import java.io.Serializable;

/**
 * Class to represent a Specific class-type (dummy like). Used for Java-Reflection, where a parameter is null.
 * 
 * @author Oliver
 * 
 * @param <T>
 */
public class NullObject<T> implements Serializable {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = 2385996165411626942L;

	/**
	 * The {@link NullObject}s class type.
	 */
	private final Class<T> parameterClass;

	/**
	 * Creates a new {@link NullObject}.
	 * 
	 * @param nullObjectClass
	 *            the {@link NullObject}s class
	 */
	public NullObject(Class<T> nullObjectClass) {
		super();
		this.parameterClass = nullObjectClass;
	}

	/**
	 * Returns the parameterClass
	 * 
	 * @return
	 */
	public Class<T> getParameterClass() {
		return this.parameterClass;
	}
}

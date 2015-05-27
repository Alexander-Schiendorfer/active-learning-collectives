package utilities.parameters;

/**
 * This {@link RuntimeException} is thrown if a parameter that is accessed via {@link Parameters} is not available.
 */
public class ParameterNotAvailableException extends RuntimeException {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = -8314273891702297081L;

	/**
	 * Creates a new {@link ParameterNotAvailableException}.
	 */
	public ParameterNotAvailableException() {
	}

	/**
	 * Creates a new {@link ParameterNotAvailableException}.
	 *
	 * @param message
	 *            the error message
	 */
	public ParameterNotAvailableException(String message) {
		super(message);
	}
}
package utilities.parameters;

/**
 * Interface for classes that provide access to parameters that might be outdated at some point of time and thus need to
 * be updated.
 */
public interface IParameterChangeListener {

	/**
	 * Refreshes the parameters provided by this {@link IParameterChangeListener}.
	 */
	public void refreshParameters();

}
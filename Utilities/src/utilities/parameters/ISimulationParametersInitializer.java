package utilities.parameters;

/**
 * Interface for classes that initialize simulation {@link Parameters}.
 */
public interface ISimulationParametersInitializer {

	/**
	 * Initializes a {@link Parameters} object that provides access to the available simulation parameters. To
	 * initialize it, classes that implement this interface have to build up a {@link Parameters} object that basically
	 * holds key-value-pairs for the available parameters.
	 *
	 * @return the initialized {@link Parameters}
	 */
	public Parameters initializeSimulationParameters();

}
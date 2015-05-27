package utilities.parameters;

/**
 * A concrete {@link ISimulationParametersInitializer} that initializes the {@link Parameters} obtained via an XML file
 * and parsed by an {@link XMLParameterParser}.
 */
public class XMLSimulationParametersInitializer implements ISimulationParametersInitializer {

	/**
	 * Path to the params xml file with the specified parameters for the simulation.
	 */
	private final String paramsFilePath;

	// /**
	// * Creates an {@link XMLSimulationParametersProviderInitializer} that uses the path of the default xml parameters
	// * file as specified in {@link #PARAMS_FILE_PATH}.
	// */
	// public XMLSimulationParametersProviderInitializer() {
	// // use the default params file name
	// }

	/**
	 * Creates an {@link XMLSimulationParametersInitializer} that uses the given xml parameters file.
	 *
	 * @param paramsFilePath
	 *            the path to the xml parameters file
	 */
	public XMLSimulationParametersInitializer(String paramsFilePath) {
		this.paramsFilePath = paramsFilePath;
	}

	@Override
	public Parameters initializeSimulationParameters() {
		XMLParameterParser xmlParser = new XMLParameterParser(this.paramsFilePath);
		return xmlParser.getParameters();
	}
}
package utilities.parameters;

/**
 * {@link ISimulationParametersInitializer} that "dummy initializes" the {@link SimulationParameters} by returning an
 * empty {@link Parameters} object.
 */
public class DummySimulationParametersInitializer implements ISimulationParametersInitializer {

	@Override
	public Parameters initializeSimulationParameters() {
		return new Parameters();
	}
}
package utilities.parameters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Extension of {@link SimulationParameters} that provides the possibility to modify the {@link Parameters} currently in
 * use.
 *
 * <br>
 * <br>
 * Be aware that local or static variables holding a value previously obtained via the {@link SimulationParameters} do
 * not change! Only the (new) access via {@link SimulationParameters} then retrieves the modified parameter.
 */
public class SimulationParametersModifier extends SimulationParameters {

	/**
	 * For logging purposes.
	 */
	private static final Logger LOG = LogManager.getLogger();

	/**
	 * Private constructor to prevent instantiation.
	 */
	private SimulationParametersModifier() {

	}

	/* ***************
	 * Other Getters *
	 *****************/

	/**
	 * Returns the used {@link Parameters}. The {@link SimulationParameters} must be in the modify state to access the
	 * {@link Parameters}, else an {@link IllegalStateException} is thrown.
	 *
	 * <br>
	 * <br>
	 * Be aware that local or static variables holding a value previously obtained via the {@link SimulationParameters}
	 * do not change! Only the (new) access via {@link SimulationParameters} then retrieves the modified parameter.
	 *
	 * @return the used {@link Parameters} that can be modified
	 * @throws IllegalStateException
	 *             if the {@link SimulationParameters} are not in the modify state
	 */
	public static Parameters getParameters() {
		if (!SimulationParameters.modifyingSimulationParametersState) {
			throw SimulationParametersModifier.LOG.throwing(new IllegalStateException(
					"To get access to the parameters, first the modify state must be entered."));
		}
		return SimulationParameters.myParamProvider.getParameters();
	}

	/**
	 * Enters the modify state and thus enables the modification of the {@link Parameters}.
	 */
	public static void enterModifyState() {
		if (SimulationParameters.modifyingSimulationParametersState) {
			SimulationParametersModifier.LOG.warn("SimulationParameters are already in the modify state. SimulationParameters remain in this state.");
		}
		SimulationParameters.modifyingSimulationParametersState = true;
	}

	/**
	 * Leaves the modify state and thus disables the modification of the {@link Parameters}.
	 */
	public static void leaveModifyState() {
		if (!SimulationParameters.modifyingSimulationParametersState) {
			SimulationParametersModifier.LOG.warn("SimulationParameters are not in the modify state. SimulationParameters remain in this state.");
		}
		SimulationParameters.modifyingSimulationParametersState = false;
	}
}
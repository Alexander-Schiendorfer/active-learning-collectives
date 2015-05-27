package utilities.parameters;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import utilities.parameters.sweepfilesgenerator.SimulationParametersExporter;

/**
 * Holds parameters used for this simulation run.
 */
public class Parameters {

	/**
	 * For logging purposes.
	 */
	private final static Logger LOG = LogManager.getLogger();

	/**
	 * Holds the parameters. Maps a parameter name to its value.
	 *
	 * Uses a {@link LinkedHashMap} to guarantee the insertion order when iterating over the map which is needed for the
	 * ordered export of the parameters (see {@link SimulationParametersExporter}).
	 */
	protected final Map<String, Object> parameters = new LinkedHashMap<String, Object>();

	/**
	 * Returns the parameter value of the parameter with the given name.
	 *
	 * @param parameterName
	 *            the name of the parameter to get
	 * @return the value of the parameter with the given name
	 * @throws ParameterNotAvailableException
	 *             if the parameter with the given name is not available
	 */
	public Object getValue(String parameterName) throws ParameterNotAvailableException {
		if (this.parameters.containsKey(parameterName)) {
			return this.parameters.get(parameterName);
		}
		throw new ParameterNotAvailableException("No parameter value for the given parameter name '" + parameterName + "' available!");
	}

	/**
	 * Sets a parameter value for a parameter name. If a value for the name already exists, the old value is replaced.
	 * Empty parameter names or parameter names that are <code>null</code> are not valid and thus lead to an
	 * {@link IllegalArgumentException}.
	 *
	 * @param parameterName
	 *            the name of the parameter
	 * @param parameterValue
	 *            the value of the parameter
	 * @return <code>true</code> if the parameter did not already exist or was <code>null</code> before,
	 *         <code>false</code> otherwise
	 * @throws IllegalArgumentException
	 *             If the <code>parameterName</code> is empty or <code>null</code>
	 */
	public boolean setParameter(String parameterName, Object parameterValue) {
		if (parameterName == null || parameterName.isEmpty()) {
			throw Parameters.LOG.throwing(new IllegalArgumentException("Invalid parameter name '" + parameterName
					+ "'! The parameter name must not be null or empty!"));
		}
		if (this.parameters.containsKey(parameterName)) {
			Parameters.LOG.warn("The parameter '" + parameterName + "' was already present with the value '" + this.parameters.get(parameterName)
					+ "'. The old value is now replaced with the new value '" + parameterValue + "'!");
		}
		return this.parameters.put(parameterName, parameterValue) == null;
	}

	/**
	 * Checks if the parameter with the given parameter name is available, i.e., checks if there is an entry in the
	 * parameters map with the given parameter name.
	 *
	 * @param parameterName
	 *            the name of the parameter to check
	 * @return <code>true</code> if there already exists a parameter with the given parameter name, <code>false</code>
	 *         if not
	 */
	public boolean containsParameter(String parameterName) {
		return this.parameters.containsKey(parameterName);
	}

	/**
	 * Adds, i.e., merges, the given {@link Parameters} with <code>this</code>. Already existing parameters with the
	 * same name are replaced.
	 *
	 * @param otherParameters
	 *            the {@link Parameters} to add to <code>this</code>
	 */
	public void addAllParameters(Parameters otherParameters) {
		// instead of simply using the addAll method of the map, set the parameter one after another to have checks as
		// given in the setParameter method (such as a replacement of an already existing parameter)
		for (String parameterName : otherParameters.parameters.keySet()) {
			this.setParameter(parameterName, otherParameters.parameters.get(parameterName));
		}
	}

	/**
	 * Returns all parameter names.
	 *
	 * @return all parameter names
	 */
	public Set<String> keySet() {
		return this.parameters.keySet();
	}
}
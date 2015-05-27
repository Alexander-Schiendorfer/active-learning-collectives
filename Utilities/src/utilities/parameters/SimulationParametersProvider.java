package utilities.parameters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class that provides access to the simulation parameters.
 */
public class SimulationParametersProvider {

	/**
	 * For logging purposes.
	 */
	private final static Logger LOG = LogManager.getLogger();

	/**
	 * Holds all available simulation parameters.
	 */
	private final Parameters parameters;

	/**
	 * Creates a new {@link SimulationParametersProvider} with the given {@link Parameters}.
	 *
	 * @param parameters
	 *            the {@link Parameters} to use
	 */
	public SimulationParametersProvider(Parameters parameters) {
		this.parameters = parameters;
	}

	/**
	 * Checks if a given parameter is present.
	 *
	 * @param parameterName
	 *            the name of the parameter to check for its presence.
	 * @return <code>true</code> if the parameter is present and has a non-null value, <code>false</code> otherwise
	 */
	public boolean exists(String parameterName) {
		return this.parameters.containsParameter(parameterName);
	}

	/**
	 * Gets the value of a parameter identified by its name. Returns {@link Double} values.
	 *
	 * @param parameterName
	 *            the name of the parameter
	 * @return a {@link Double} representing the value of the parameter or {@link Double#NaN} if an error occurred
	 * @throws ParameterNotAvailableException
	 *             if the parameter wasn't found
	 */
	public Double getDoubleParameter(String parameterName) throws ParameterNotAvailableException {
		Double value = Double.NaN;

		Object param = this.parameters.getValue(parameterName);
		if (param == null) {
			value = null;
		} else if ((param.getClass().equals(java.lang.Double.class)) || (param.getClass().equals(java.lang.Integer.class))) {
			value = (Double) param;
		} else {
			SimulationParametersProvider.LOG.error("Expected value '" + param + "' of parameter '" + parameterName
					+ "' to be defined as double, which it is, however, not! Returning Double.NaN.");
		}

		return value;
	}

	/**
	 * Gets the value of a parameter identified by its name. Returns {@link Integer} values.
	 *
	 * @param parameterName
	 *            the name of the parameter
	 * @return an {@link Integer} representing the value of the parameter or <code>null</code> if an error occurred
	 * @throws ParameterNotAvailableException
	 *             if the parameter wasn't found
	 */
	public Integer getIntParameter(String parameterName) throws ParameterNotAvailableException {
		Integer value = null;

		Object param = this.parameters.getValue(parameterName);
		if (param == null) {
			value = null;
		} else if (param.getClass().equals(java.lang.Integer.class)) {
			value = (Integer) param;
		} else {
			SimulationParametersProvider.LOG.error("Expected value '" + param + "' of parameter '" + parameterName
					+ "' to be defined as integer, which it is, however, not! Returning null.");
		}

		return value;
	}

	/**
	 * Gets the value of a parameter identified by its name. Returns {@link Long} values.
	 *
	 * @param parameterName
	 *            the name of the parameter
	 * @return a {@link Long} representing the value of the parameter or <code>null</code> if an error occurred
	 * @throws ParameterNotAvailableException
	 *             if the parameter wasn't found
	 */
	public Long getLongParameter(String parameterName) throws ParameterNotAvailableException {
		Long value = null;

		Object param = this.parameters.getValue(parameterName);
		if (param == null) {
			value = null;
		} else if (param.getClass().equals(Long.class) || param.getClass().equals(Integer.class)) {
			value = new Long(param.toString());
		} else {
			SimulationParametersProvider.LOG.error("Expected value '" + param + "' of parameter '" + parameterName
					+ "' to be defined as Long, which it is, however, not! Returning null.");
		}

		return value;
	}

	/**
	 * Gets the value of a parameter identified by its name. Returns {@link Boolean} values.
	 *
	 * @param parameterName
	 *            the name of the parameter
	 * @return a {@link Boolean} representing the value of the parameter or <code>null</code> if an error occurred
	 * @throws ParameterNotAvailableException
	 *             if the parameter wasn't found
	 */
	public Boolean getBoolean(String parameterName) throws ParameterNotAvailableException {
		Boolean value = null;

		Object param = this.parameters.getValue(parameterName);
		if (param == null) {
			value = null;
		} else if (param.getClass().equals(Boolean.class)) {
			value = (Boolean) param;
		} else {
			SimulationParametersProvider.LOG.error("Expected value '" + param + "' of parameter '" + parameterName
					+ "' to be defined as boolean, which it is, however, not! Returning null.");
		}

		return value;
	}

	/**
	 * Gets the value of a parameter identified by its name. Returns {@link String} values.
	 *
	 * @param parameterName
	 *            the name of the parameter
	 * @return a {@link String} representing the value of the parameter or <code>null</code> if an error occurred
	 * @throws ParameterNotAvailableException
	 *             if the parameter wasn't found
	 */
	public String getString(String parameterName) throws ParameterNotAvailableException {
		String value = null;

		Object param = this.parameters.getValue(parameterName);
		if (param == null) {
			value = null;
		} else if (param.getClass().equals(String.class)) {
			value = (String) param;
		} else {
			SimulationParametersProvider.LOG.error("Expected value '" + param + "' of parameter '" + parameterName
					+ "' to be defined as string, which it is, however, not! Returning null.");
		}

		return value;
	}

	/**
	 * Gets the value of the parameter identified by its name. Returns an array of {@link String} values. The value of
	 * the parameter must be a {@link String}, separating the single {@link String} elements by a ";".
	 *
	 * @param parameterName
	 *            the name of the parameter
	 * @return an array of {@link String}s representing the value of the parameter or <code>null</code> if an error
	 *         occurred
	 * @throws ParameterNotAvailableException
	 *             if the parameter wasn't found
	 */
	public String[] getStrings(String parameterName) throws ParameterNotAvailableException {
		String string = this.getString(parameterName);
		if (string != null) {
			return string.split(";");
		} else {
			return null;
		}
	}

	/**
	 * Gets the value of the parameter identified by its name. Returns an array of {@link Double} values. The value of
	 * the parameter must be a {@link String}, separating the single {@link Double} elements by a ";".
	 *
	 * @param parameterName
	 *            the name of the parameter
	 * @return an array of {@link Double}s representing the value of the parameter or <code>null</code> if an error
	 *         occurred
	 * @throws ParameterNotAvailableException
	 *             if the parameter wasn't found
	 */
	public Double[] getDoubles(String parameterName) throws ParameterNotAvailableException {
		String string = this.getString(parameterName);
		if (string != null) {
			String[] strings = string.split(";");
			Double[] doubles = new Double[strings.length];

			int index = 0;
			for (String aString : strings) {
				doubles[index++] = Double.parseDouble(aString);
			}

			return doubles;
		} else {
			return null;
		}
	}

	/**
	 * Gets an instance of an Object whose class is identified by the parameter. Returns {@link Object} values.
	 *
	 * @param parameterName
	 *            the name of the parameter that contains the class name
	 * @return an {@link Object} instance of the class identified by the parameter or <code>null</code> if the parameter
	 *         wasn't found
	 * @throws ParameterNotAvailableException
	 *             if the parameter wasn't found
	 */
	@SuppressWarnings("unchecked")
	public Object getObject(String parameterName) throws ParameterNotAvailableException {
		String className = (String) this.parameters.getValue(parameterName);
		if (className != null) {
			try {
				Class<Object> classToLoad = (Class<Object>) Class.forName(className);
				Object obj = classToLoad.newInstance();
				return obj;
			} catch (InstantiationException e) {
				SimulationParametersProvider.LOG.error("Cannot instantiate class '" + className + "'!", e);
			} catch (IllegalAccessException e) {
				SimulationParametersProvider.LOG.error("Constructor for '" + className + "' is inaccessible!", e);
			} catch (ClassNotFoundException e) {
				SimulationParametersProvider.LOG.error("Unable to find class '" + className + "'!", e);
			}
		}
		return null;
	}

	/**
	 * Gets an instance of an Object whose class is identified by the parameter and calls the constructor as identified
	 * by the provided classes and instance parameters. Returns {@link Object} values.
	 *
	 * @param parameterName
	 *            the name of the parameter that contains the class name
	 * @param classes
	 *            the classes of the parameters given to the constructor
	 * @param instanceParameters
	 *            the values of the parameters given to the constructor
	 * @return an {@link Object} instance of the class identified by the parameter and initialized with the constructor
	 *         the fits the given classes or <code>null</code> if the parameter wasn't found
	 * @throws ParameterNotAvailableException
	 *             if the parameter wasn't found
	 */
	@SuppressWarnings("unchecked")
	public Object getObject(String parameterName, Class<?>[] classes, Object[] instanceParameters) throws ParameterNotAvailableException {
		String className = (String) this.parameters.getValue(parameterName);

		if (className != null) {
			try {
				Class<Object> classToLoad = (Class<Object>) Class.forName(className);
				Constructor<Object> c = classToLoad.getConstructor(classes);
				Object object = c.newInstance(instanceParameters);
				return object;
			} catch (ClassNotFoundException e) {
				SimulationParametersProvider.LOG.error("Unable to find class " + className, e);
			} catch (SecurityException e) {
				SimulationParametersProvider.LOG.error("Unable to access constructor or package for " + className, e);
			} catch (NoSuchMethodException e) {
				SimulationParametersProvider.LOG.error("Constructor not found.", e);
			} catch (InstantiationException e) {
				SimulationParametersProvider.LOG.error("Class " + className + " is abstract", e);
			} catch (IllegalAccessException e) {
				SimulationParametersProvider.LOG.error("Constructor for " + className + " is inaccessible", e);
			} catch (InvocationTargetException e) {
				SimulationParametersProvider.LOG.error("Constructor for " + className + " threw an exception:");
				SimulationParametersProvider.LOG.error(e.getMessage(), e);
			}
		}
		return null;
	}

	/**
	 * Returns the {@link Parameters} for this simulation run.
	 *
	 * @return the {@link Parameters} for this simulation run.
	 */
	public Parameters getParameters() {
		return this.parameters;
	}
}
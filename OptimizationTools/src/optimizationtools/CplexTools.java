package optimizationtools;

import utilities.RoundingHelper;

/**
 * Provides basic operations in order to exchange data between the simulation environment and CPLEX.
 *
 * @author Gerrit
 *
 */
public class CplexTools {

	/**
	 * List of chars that must not be included in strings forwarded to CPLEX.
	 */
	public final static String[] INCOMPATIBLE_CHARS = new String[] { "#", ".", "-", "+", "~", "@" };

	/**
	 * 0.0+-DOUBLE_EQUAL_THRESHOLD is considered 0.0 in proposed outputs. This is needed because of floating point
	 * precision errors. Else, there can be an infinite number of iterations in the bidding and selection phase or the
	 * filtering functions will return strange results.
	 */
	public final static double DOUBLE_EQUAL_THRESHOLD = 0.00001;

	/**
	 * Creates a string that is compatible with CPLEX's data input language. Adds a letter in the beginning of the
	 * string.
	 *
	 * @param originalString
	 *            the original string
	 * @return a string compatible with CPLEX
	 */
	public static String createCplexCompatibleString(String originalString) {
		String returnString = originalString;

		for (String s : CplexTools.INCOMPATIBLE_CHARS) {
			returnString = returnString.replace(s, "_");
		}

		return "x_" + returnString;
	}

	/**
	 * Handles CPLEX floating point precision errors. <br/>
	 * Converts the given value into a double with a predefined precision.
	 *
	 * @param value
	 *            the value to be converted
	 * @return the converted value
	 */
	public static double convertJavaDoubleIntoCplexFloat(double value) {
		// TruCAOS needs a scale of 1 -- the regio-central approach is able to deal with scales of 4 (a scale of 1
		// prevents numerical errors in CPLEX)
		// return RoundingHelper.roundToScale(value, 4);
		return RoundingHelper.roundToScale(value, 1);
	}

	/**
	 * Handles CPLEX floating point precision errors. <br/>
	 * Converts the given values into a double array with a predefined precision.
	 *
	 * @param values
	 *            the array to be converted
	 * @return the converted values
	 */
	public static double[] convertJavaDoubleArrayIntoCplexFloatArray(double[] values) {
		double[] convertedValues = new double[values.length];

		for (int i = 0; i < values.length; i++)
			convertedValues[i] = CplexTools.convertJavaDoubleIntoCplexFloat(values[i]);

		return convertedValues;
	}

	/**
	 * Handles CPLEX floating point precision errors at predefined precision CplexTools.DOUBLE_EQUAL_THRESHOLD. <br/>
	 * Converts the given value into a double so that <code>value <= max && (value >= min || value == 0.0)</code> holds.
	 *
	 * @param min
	 *            the value's min value
	 * @param max
	 *            the value's max value
	 * @param value
	 *            the value to be converted
	 * @return the converted value
	 */
	public static double convertCplexFloatIntoJavaDouble(double min, double max, double value) {
		return convertCplexFloatIntoJavaDouble(min, max, value, CplexTools.DOUBLE_EQUAL_THRESHOLD);
	}

	/**
	 * Handles CPLEX floating point precision errors at arbitrary precision. <br/>
	 * Converts the given value into a double so that <code>value <= max && (value >= min || value == 0.0)</code> holds.
	 *
	 * @param min
	 *            the value's min value
	 * @param max
	 *            the value's max value
	 * @param value
	 *            the value to be converted
	 * @return the converted value
	 */
	public static double convertCplexFloatIntoJavaDouble(double min, double max, double value, double precision) {
		// handle CPLEX floating point precision errors
		if (min > value && value > min - CplexTools.DOUBLE_EQUAL_THRESHOLD)
			value = min;
		if (value != 0.0 && Math.abs(value) < CplexTools.DOUBLE_EQUAL_THRESHOLD)
			value = 0.0;
		if (value > max && value < max + CplexTools.DOUBLE_EQUAL_THRESHOLD)
			value = max;

		return value;
	}

	/**
	 * Checks if the <code>val1</code> and <code>val2</code> differ more than {@link #DOUBLE_EQUAL_THRESHOLD}. If they
	 * do or if the difference is equal to {@link #DOUBLE_EQUAL_THRESHOLD}, <code>val2</code> is returned. Else
	 * <code>val1</code> is returned.
	 *
	 * @param val1
	 *            the first value
	 * @param val2
	 *            the second value
	 * @return <code>val1</code> or <code>val2</code> (see above).
	 */
	public static double identifyEqualCplexFloats(double val1, double val2) {
		if (Math.abs(val1 - val2) < CplexTools.DOUBLE_EQUAL_THRESHOLD)
			return val1;

		return val2;
	}

	/**
	 * Converts a CPLEX int that is provided by the CPLEX JAVA API in form of a JAVA double (i.e., actually an imprecise
	 * CPLEX float) into a JAVA int by rounding it half-way up, using a scale of zero, to a JAVA int.
	 *
	 * @param value
	 *            the value that actually represents an int
	 * @return the int interpretation
	 */
	public static int convertCplexPseudoFloatIntoJavaInt(double value) {
		return (int) RoundingHelper.roundToScale(value, 0);
	}

	/**
	 * Converts a CPLEX boolean that is provided by the CPLEX JAVA API in form of a JAVA double (i.e., actually an
	 * imprecise CPLEX float) into a JAVA boolean by rounding it half-way up, using a scale of zero, to a JAVA double
	 * and comparing it to 1.0.
	 *
	 * @param value
	 *            the value that actually represents a boolean
	 * @return the boolean interpretation
	 */
	public static boolean convertCplexPseudoFloatIntoJavaBoolean(double value) {
		return RoundingHelper.roundToScale(value, 0) == 1.0;
	}
}

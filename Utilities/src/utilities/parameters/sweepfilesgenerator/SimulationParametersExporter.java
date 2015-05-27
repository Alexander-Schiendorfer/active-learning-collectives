package utilities.parameters.sweepfilesgenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import utilities.parameters.Parameters;
import utilities.parameters.SimulationParameters;
import utilities.parameters.SimulationParametersProvider;
import utilities.parameters.XMLParameterParser;

/**
 * Class that provides an interface to export the {@link SimulationParameters} to an xml file that can then afterwards
 * be parsed by the {@link XMLParameterParser} for, e.g., an evaluation.
 */
public class SimulationParametersExporter extends SimulationParameters {

	/**
	 * For logging purposes.
	 */
	private static final Logger LOG = LogManager.getLogger();

	/**
	 * Private constructor to prevent instantiation.
	 */
	private SimulationParametersExporter() {

	}

	/* ********
	 * Export *
	 **********/

	/**
	 * Exports the {@link SimulationParameters} to an xml file with the given path. The exported file is compatible to
	 * the {@link XMLParameterParser} to enable an import of the parameters, for example, in an evaluation.
	 *
	 * @param xmlFilePath
	 *            the path of the xml file where to export the {@link SimulationParameters}
	 */
	public static void exportSimulationParametersToXml(String xmlFilePath) {
		SimulationParametersExporter.exportSimulationParametersToXml(xmlFilePath, "", SimulationParameters.myParamProvider.getParameters());
	}

	/**
	 * Exports the {@link Parameters} provided by the given {@link SimulationParametersProvider} to an xml file with the
	 * given path. The file can then afterwards be parsed by the {@link XMLParameterParser} for, e.g., an evaluation.
	 *
	 * @param xmlFilePath
	 *            the path of the xml to generate
	 * @param additionalSection
	 *            a String holding additional information, e.g, a config section, that is placed at the beginning of the
	 *            generated sweep file
	 * @param parameters
	 *            all {@link Parameters} to be exported
	 */
	public static void exportSimulationParametersToXml(String xmlFilePath, String additionalSection, Parameters parameters) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(xmlFilePath)));

			// start with sweep
			out.append("<sweep>\n");

			// append the additional section
			out.append(additionalSection + "\n");

			// append every parameter
			for (String parameterName : parameters.keySet()) {
				SimulationParametersExporter.appendParameterString(out, parameterName, parameters.getValue(parameterName));
			}

			// end with sweep
			out.append("</sweep>");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Appends a parameter string in the necessary xml format to the given {@link PrintWriter} for the given parameter
	 * name and parameter value.
	 *
	 * @param out
	 *            the {@link PrintWriter} to append the string to
	 * @param parameterName
	 *            the name of the parameter
	 * @param parameterValue
	 *            the value of the parameter
	 */
	private static void appendParameterString(PrintWriter out, String parameterName, Object parameterValue) {
		// first: derive the type of the parameter (can be string, number, or boolean)
		String constantType = "";
		if (parameterValue instanceof String) {
			constantType = "string";
		} else if (parameterValue instanceof Number) {
			constantType = "number";
		} else if (parameterValue instanceof Boolean) {
			constantType = "boolean";
		} else {
			throw SimulationParametersExporter.LOG.throwing(new IllegalArgumentException("Value '" + parameterValue + "' of parameter '" + parameterName
					+ "' has an unknown parameter type (should be String, Number, or Boolean)!"));
		}
		// append parameter
		out.append("<parameter name=\"" + parameterName + "\" type=\"constant\" constant_type=\"" + constantType + "\" value=\"" + parameterValue + "\"/>\n");
	}
}
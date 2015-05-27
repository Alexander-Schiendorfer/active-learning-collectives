package utilities.parameters;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * A {@link DefaultHandler2} to parse the parameter xml file.
 */
public class XMLParameterParser extends DefaultHandler2 {

	/**
	 * For logging purposes.
	 */
	private final static Logger LOG = LogManager.getLogger();

	/**
	 * Parsed {@link String} representations of a parameter value (i.e., the string in the xml parameter file that
	 * defines the parameter value) that equal this {@link String} are interpreted to have <code>null</code> as
	 * parameter value in {@link Parameters}.
	 */
	private final static String NULL_PARAMETER_STRING = "null";

	/**
	 * Holds the name of the xml parameter file.
	 */
	private final String parameterFile;

	/**
	 * Holds the parsed {@link Parameters}. If no parsing has yet taken place, this value is <code>null</code>.
	 */
	private Parameters parameters = null;

	/**
	 * Creates a new {@link XMLParameterParser}.
	 *
	 * @param parameterFile
	 *            the name of the xml parameter file
	 */
	public XMLParameterParser(String parameterFile) {
		this.parameterFile = parameterFile;
	}

	/**
	 * @return Returns the parsed {@link Parameters} for the xml file {@link #parameterFile}.
	 */
	public Parameters getParameters() {
		if (this.parameters == null) {
			this.parseParameters();
		}
		return this.parameters;
	}

	/**
	 * Parses the xml file {@link #parameterFile} and fills the {@link #parameters}.
	 */
	private void parseParameters() {
		// get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			this.parameters = new Parameters();

			// get a new instance of parser
			SAXParser sp = spf.newSAXParser();
			sp.setProperty("http://xml.org/sax/properties/lexical-handler", this);

			// parse the file and also register this class for call backs
			sp.parse(new File(this.parameterFile), this);

		} catch (SAXException se) {
			se.printStackTrace();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("sweep")) {
			// do nothing
		} else if (qName.equalsIgnoreCase("parameter")) {
			String parameterName = attributes.getValue("name");
			String parameterType = attributes.getValue("constant_type");
			String parameterValueString = attributes.getValue("value");
			// in case the name/type/value are unavailable, we return
			if (parameterName == null || parameterType == null || parameterValueString == null) {
				XMLParameterParser.LOG.warn("One of the necessary attributes seems to be unavailable. Parameter name: '" + parameterName
						+ "', parameter type: '" + parameterType + "', parameter value: '" + parameterValueString
						+ "'. Necessary attributes are 'name', 'constant_type', and 'value'.");
				return;
			}

			if (!parameterValueString.equals(XMLParameterParser.NULL_PARAMETER_STRING)) {
				// create the right parameter value object dependent on the specified type
				Object parameterValue = null;
				// String
				if (parameterType.equalsIgnoreCase("string")) {
					// this is easy, the value is already a String
					parameterValue = parameterValueString;
				}
				// Boolean
				else if (parameterType.equalsIgnoreCase("boolean")) {
					if (!parameterValueString.equalsIgnoreCase("true") && !parameterValueString.equalsIgnoreCase("false")) {
						XMLParameterParser.LOG.error("Parameter '" + parameterName + "' has not a valid boolean value ('true' or 'false')! Given value is '"
								+ parameterValueString + "'. Parameter is ignored.");
						return;
					}
					parameterValue = Boolean.parseBoolean(parameterValueString);

				}
				// Number (Integer or Double)
				else if (parameterType.equalsIgnoreCase("number")) {
					try {
						// integer parameter
						parameterValue = Integer.parseInt(parameterValueString);

					} catch (NumberFormatException e) {
						try {
							// double parameter
							parameterValue = Double.parseDouble(parameterValueString);
						} catch (NumberFormatException e2) {
							XMLParameterParser.LOG.error("Parameter '" + parameterName + "' is not a valid number! Given value is '" + parameterValueString
									+ "'. Parameter is ignored.");
							return;
						}
					}
				}
				// invalid type
				else {
					XMLParameterParser.LOG.error("Parameter '" + parameterName + "' is not of valid type ('string', 'boolean', or 'number')! Given type is '"
							+ parameterType + "'. Parameter is ignored.");
					return;
				}

				// put the parameter in the map
				this.parameters.setParameter(parameterName, parameterValue);
			} else { // parameter value equals null string
				// put the parameter with null value in the map
				this.parameters.setParameter(parameterName, null);
			}
		} else if (qName.equalsIgnoreCase("config") || qName.equalsIgnoreCase("batchruns") || qName.equalsIgnoreCase("setting")) {
			// these are settings for running the simulation via JPPF -- these are ok and ignore them here
		}

		// invalid tag
		else {
			XMLParameterParser.LOG.error("The xml file only expects tags that are 'sweep' or 'parameter'. Given tag is '" + qName + "'.");
		}
	}

	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		// TODO
		// System.out.println("XML comment: " + new String(ch, start, length));
	}
}
package utilities.parameters.sweepfilesgenerator;

import java.util.HashSet;
import java.util.Set;

import utilities.parameters.DummySimulationParametersInitializer;
import utilities.parameters.Parameters;
import utilities.parameters.SimulationParameters;
import utilities.parameters.XMLSimulationParametersInitializer;

/**
 * Class to generate sweep files with a set of given {@link SweepfileConfig}s based on a given params xml template.
 */
public class SweepfilesGenerator {

	/**
	 * The name of the parameter file template that the sweep files are based on.
	 */
	private final String inputTemplateFileName;

	/**
	 * Holds all configs that are processed to generate sweep files.
	 */
	private final Set<SweepfileConfig> configs;

	/**
	 * The file extension of the generated files with a leading dot, e.g., ".xml".
	 */
	private final String fileExtension;

	/**
	 * A String holding additional information, e.g, a config section, that is placed at the beginning of the generated
	 * sweep file.
	 */
	private final String additionalSection;

	/**
	 * Creates a new {@link SweepfilesGenerator} with the given arguments.
	 *
	 * @param inputTemplateFileName
	 *            the name of the input template file that the sweep files are based on
	 * @param fileExtension
	 *            the file extension of the generated files with a leading dot, e.g., ".xml"
	 * @param configs
	 *            all {@link SweepfileConfig}s used to generate the sweep files
	 */
	public SweepfilesGenerator(String inputTemplateFileName, String fileExtension, Set<SweepfileConfig> configs) {
		this(inputTemplateFileName, fileExtension, "", configs);
	}

	/**
	 * Creates a new {@link SweepfilesGenerator} with the given arguments.
	 *
	 * @param inputTemplateFileName
	 *            the name of the input template file that the sweep files are based on
	 * @param fileExtension
	 *            the file extension of the generated files with a leading dot, e.g., ".xml"
	 * @param additionalSection
	 *            a String holding additional information, e.g, a config section, that is placed at the beginning of the
	 *            generated sweep file
	 * @param configs
	 *            all {@link SweepfileConfig}s used to generate the sweep files
	 */
	public SweepfilesGenerator(String inputTemplateFileName, String fileExtension, String additionalSection, Set<SweepfileConfig> configs) {
		this.inputTemplateFileName = inputTemplateFileName;
		this.fileExtension = fileExtension;
		this.configs = configs;
		this.additionalSection = additionalSection;
	}

	public void generateSweepfiles() {
		System.out.println("Generating sweep files...");

		// basic parameter initialization with the template file
		SimulationParameters.destroy();
		SimulationParameters.init(new DummySimulationParametersInitializer());

		// process all configs
		for (SweepfileConfig config : this.configs) {
			Set<ParameterToValues> parameterSpace = config.getParameterSpace();

			// determine the cartesian product of all parameter combinations of this config
			// to determine the cartesian product, we have to create an array of parameter names to values
			Set<Parameter>[] parameterValuesArray = (Set<Parameter>[]) new Set<?>[parameterSpace.size()];
			int i = 0;
			for (ParameterToValues parameterToValues : parameterSpace) {
				parameterValuesArray[i++] = parameterToValues.generateParameterValues();
			}
			Set<Set<Parameter>> parametrizations = this.cartesianProduct(parameterValuesArray);

			Parameters params = new XMLSimulationParametersInitializer(this.inputTemplateFileName).initializeSimulationParameters();
			// generate all sweep files
			for (Set<Parameter> parametrization : parametrizations) {
				// set all parameters (i.e., modify the blue print parameters)
				for (Parameter parameter : parametrization) {
					params.setParameter(parameter.name, parameter.value);
				}
				// get the sweep file name once as multiple calls could lead to different names, e.g., when using the
				// current system time
				String sweepFileName = config.getSweepfileName(params);
				// set the eval directory parameter
				params.setParameter(config.getEvalExportDirectoryParametername(), sweepFileName);
				// export parameter file
				SimulationParametersExporter.exportSimulationParametersToXml(sweepFileName + this.fileExtension, this.additionalSection, params);
			}
		}

		System.out.println("Generation of sweep files finished!");
	}

	/**
	 * @param parameterValuesArray
	 * @return
	 *
	 */
	private Set<Set<Parameter>> cartesianProduct(Set<Parameter>... parameterValuesArray) {
		// if (parameterValuesArray.length < 2)
		// throw new IllegalArgumentException("Can't have a product of fewer than two sets (got " +
		// parameterValuesArray.length + ")");

		return this.cartesianProduct(0, parameterValuesArray);
	}

	private Set<Set<Parameter>> cartesianProduct(int index, Set<Parameter>... parameterValuesArray) {
		Set<Set<Parameter>> ret = new HashSet<Set<Parameter>>();
		if (index == parameterValuesArray.length) {
			ret.add(new HashSet<Parameter>());
		} else {
			for (Parameter parameter : parameterValuesArray[index]) {
				for (Set<Parameter> set : this.cartesianProduct(index + 1, parameterValuesArray)) {
					set.add(parameter);
					ret.add(set);
				}
			}
		}
		return ret;
	}
}
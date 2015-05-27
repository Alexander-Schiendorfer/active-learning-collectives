package utilities.parameters.sweepfilesgenerator;

import java.util.Set;

import utilities.parameters.Parameters;

/**
 * Class that specifies the configuration of the generation of one sweep file. In detail, it holds the parameter space,
 * the {@link ISweepfileNameGenerator}, and the parameter name of the export directory.
 */
public class SweepfileConfig {

	/**
	 * Holds the parameter space of this config. Maps a parameter name to all its values.
	 */
	private final Set<ParameterToValues> parameterSpace;

	/**
	 * The {@link ISweepfileNameGenerator} responsible for generating the sweep file's name. This name is also used for
	 * determining the name of the export directory of an evaluation run.
	 */
	private final ISweepfileNameGenerator sweepfileNameGenerator;

	/**
	 * Holds the parameter name of the parameter that is responsible for determining the export directory of an
	 * evaluation run. This parameter should normally be set to the sweep file name or something similar.
	 */
	private final String evalExportSubdirectoryParametername;

	public SweepfileConfig(Set<ParameterToValues> parameterSpace, String evalExportDirectoryParametername, ISweepfileNameGenerator sweepfileNameGenerator) {
		this.parameterSpace = parameterSpace;
		this.evalExportSubdirectoryParametername = evalExportDirectoryParametername;
		this.sweepfileNameGenerator = sweepfileNameGenerator;
	}

	/**
	 * @return the parameterSpace
	 */
	public Set<ParameterToValues> getParameterSpace() {
		return this.parameterSpace;
	}

	public String getEvalExportDirectoryParametername() {
		return this.evalExportSubdirectoryParametername;
	}

	public String getSweepfileName(Parameters parameters) {
		return this.sweepfileNameGenerator.getName(parameters);
	}
}
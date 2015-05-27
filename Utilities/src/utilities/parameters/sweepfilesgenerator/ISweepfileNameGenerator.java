package utilities.parameters.sweepfilesgenerator;

import utilities.parameters.Parameters;

/**
 * Interface for classes that should generate the name of a sweep file.
 */
public interface ISweepfileNameGenerator {

	/**
	 * Returns the name of the sweep file.
	 *
	 * @return the name of the sweep file
	 */
	public String getName(Parameters parameters);

}
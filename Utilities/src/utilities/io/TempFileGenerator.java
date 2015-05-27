package utilities.io;

import java.io.File;
import java.io.IOException;

import utilities.parameters.SimulationParameters;

/**
 * Class for creating temporary files. These are either put in the default OS temp directory or, if enabled, in the
 * directory as specified in the parameters file.
 */
public class TempFileGenerator {

	/**
	 * Indicates whether or not the OS temp directory is used.
	 */
	public static final boolean USE_OS_TEMP_FOLDER = SimulationParameters.getBoolean("system.tempFileGenerator.useOSTempDirectory", true);

	/**
	 * The name of the temp directory that is used if {@link #USE_OS_TEMP_FOLDER} is <code>false</code>.
	 */
	public static final String TEMP_DIRECTORY_NAME = SimulationParameters.getString("system.tempFileGenerator.tempDirectoryName", "temp");

	/**
	 * The temp directory that is used if {@link #USE_OS_TEMP_FOLDER} is <code>false</code>.
	 */
	public static File TEMP_DIRECTORY;

	static {
		// create the temp directory if enabled
		if (!TempFileGenerator.USE_OS_TEMP_FOLDER) {
			TempFileGenerator.TEMP_DIRECTORY = new File(TempFileGenerator.TEMP_DIRECTORY_NAME);
			TempFileGenerator.TEMP_DIRECTORY.mkdir();
		}
	}

	/**
	 * Prevent instantiation.
	 */
	private TempFileGenerator() {
	}

	/**
	 * Creates a temporary {@link File}. The created temporary file is either created in the default temp directory (as
	 * specified by the OS) or -- if enabled -- in the directory specified in the parameters file.
	 *
	 * @param prefix
	 *            the name prefix of the temporary file
	 * @param suffix
	 *            the name suffix of the temporary file (e.g., ".dat")
	 * @return the created temporary file
	 * @throws IOException
	 */
	public static File createTempFile(String prefix, String suffix) throws IOException {
		if (TempFileGenerator.USE_OS_TEMP_FOLDER)
			return File.createTempFile(prefix, suffix);
		else {
			return File.createTempFile(prefix, suffix, TempFileGenerator.TEMP_DIRECTORY);
		}
	}
}
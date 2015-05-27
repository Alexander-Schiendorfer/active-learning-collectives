package utilities.logging;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.zip.Deflater;

import org.apache.logging.log4j.core.appender.rolling.AbstractRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescription;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescriptionImpl;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.appender.rolling.action.FileRenameAction;
import org.apache.logging.log4j.core.appender.rolling.action.GZCompressAction;
import org.apache.logging.log4j.core.appender.rolling.action.ZipCompressAction;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

/**
 * A {@link RolloverStrategy} that keeps a maximum number of old log files in the log directory. If the number is
 * exceeded, the oldest ones, i.e., the ones whose file names are the lexicographic last ones, are deleted.
 */
@Plugin(name = "MaxNumberOfFilesRolloverStrategy", category = "Core", printObject = true)
public class MaxNumberOfFilesRolloverStrategy extends AbstractRolloverStrategy {

	/**
	 * The maximum number of old log files to keep in the log directory.
	 */
	private final int maxNumberOfFiles;

	/**
	 * Creates a new {@link MaxNumberOfFilesRolloverStrategy}.
	 *
	 * @param numberOfFiles
	 *            The maximum number of old log files to keep in the log directory.
	 */
	public MaxNumberOfFilesRolloverStrategy(final int numberOfFiles) {
		this.maxNumberOfFiles = numberOfFiles;
	}

	/**
	 * Perform the rollover.
	 *
	 * @param manager
	 *            The RollingFileManager name for current active log file.
	 * @return A RolloverDescription.
	 * @throws SecurityException
	 *             if an error occurs.
	 */
	@Override
	public RolloverDescription rollover(RollingFileManager manager) throws SecurityException {
		if (this.maxNumberOfFiles >= 1) {

			final StringBuilder buf = new StringBuilder();
			manager.getPatternProcessor().formatFileName(buf, 1);
			final String currentFileName = manager.getFileName();

			File currentFile = new File(currentFileName);
			File dir = currentFile.getParentFile();
			String currentFileNameWithoutEnding_temp = currentFileName.substring(0, currentFileName.lastIndexOf("."));
			final String currentFileNameWithoutEnding = currentFileNameWithoutEnding_temp.substring(currentFileName.indexOf("/") + 1,
					currentFileNameWithoutEnding_temp.length());

			// get all files in "dir" whose names begin with "currentFileNameWithoutEnding"
			File[] files = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String fileName) {
					return fileName.startsWith(currentFileNameWithoutEnding);
				}
			});

			// determine the number of files to delete
			int filesToDelete = files.length - this.maxNumberOfFiles;
			// sort lexicographically
			Arrays.sort(files);

			// delete files if necessary (keep in mind that the current file is the last one in the array)
			while (filesToDelete > 1) {
				files[files.length - 2 - filesToDelete].delete();
				filesToDelete--;
			}
			if (filesToDelete == 1) {
				files[0].delete();
			}

			// here comes the stuff to create a new "old" log file
			String renameTo = buf.toString();

			final String compressedName = renameTo;
			Action compressAction = null;
			if (renameTo.endsWith(".gz")) {
				renameTo = renameTo.substring(0, renameTo.length() - 3);
				compressAction = new GZCompressAction(new File(renameTo), new File(compressedName), true);
			} else if (renameTo.endsWith(".zip")) {
				renameTo = renameTo.substring(0, renameTo.length() - 4);
				compressAction = new ZipCompressAction(new File(renameTo), new File(compressedName), true, Deflater.DEFAULT_COMPRESSION);
			}

			final FileRenameAction renameAction = new FileRenameAction(new File(currentFileName), new File(renameTo), false);

			return new RolloverDescriptionImpl(currentFileName, false, renameAction, compressAction);
		}

		return null;
	}

	/**
	 * Creates a new {@link MaxNumberOfFilesRolloverStrategy}.
	 *
	 * @param number
	 *            The maximum number of old log files to keep in the log directory.
	 * @return the {@link MaxNumberOfFilesRolloverStrategy}
	 */
	@PluginFactory
	public static MaxNumberOfFilesRolloverStrategy createStrategy(@PluginAttribute("number") final String number) {
		AbstractRolloverStrategy.LOGGER.info("Creating new MaxNumberOfDaysRolloverStrategy with number of files " + number);
		int numberOfFiles = 0;
		try {
			numberOfFiles = Integer.parseInt(number);
		} catch (NumberFormatException e) {
			AbstractRolloverStrategy.LOGGER.error("Value of parameter 'number' is not a valid integer! Given value: " + number, e);
		}
		return new MaxNumberOfFilesRolloverStrategy(numberOfFiles);
	}
}
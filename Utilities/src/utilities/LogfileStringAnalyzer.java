package utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import utilities.io.FileAndDirectoryHelper;
import utilities.io.RecursiveFileSearcher;

public class LogfileStringAnalyzer {

	/**
	 * For logging purposes.
	 */
	protected static final Logger LOG = LogManager.getLogger();

	/**
	 * Indicates whether multiple folders that contain log files (named "logs") should be processed or if it is a single
	 * folder that is to be processed.
	 */
	private static final boolean IS_BATCH_PROCESSING = true;

	/**
	 * The directory where the log files or the folders that contain the log files (in case of batch processing) is in.
	 */
	private static final String FATHER_DIRECTORY = "W:\\SPP-Testing\\test-sequences\\spada4";

	/**
	 * If <code>true</code>, the number of log file directories (in case of batch processing) that are loaded are
	 * restricted to the number as specified in {@link #NUMBER_OF_DIRECTORIES_TO_LOAD_AT_MOST}.
	 */
	private static final boolean RESTRICT_NUMBER_OF_DIRECTORIES = false;

	/**
	 * If the number of log file directories that are loaded should be restricted (see
	 * {@link #RESTRICT_NUMBER_OF_DIRECTORIES}), this number indicates how many directories should be analyzed at most.
	 */
	private static final int NUMBER_OF_DIRECTORIES_TO_LOAD_AT_MOST = 70;

	/**
	 * Indicates whether or not the analyzation is made tick-wise or file-wise.
	 */
	private static final boolean SPLIT_ANALYZATION_INTO_TICKS = false;

	/**
	 * The String indicating a new tick. Used when {@link #SPLIT_ANALYZATION_INTO_TICKS} is <code>true</code> to split
	 * the log files into ticks.
	 */
	private static final String TICK_SPLIT_STRING = "[AgentScheduler] - Current tick";

	/**
	 * The strings that should be included in the log files or ticks. Case sensitive!!
	 */
	private static final String[] STRINGS_TO_BE_INCLUDED = { "Applied" };

	/**
	 * The strings that should not be included in the log files or ticks. Case sensitive!!
	 */
	// private static final String[] STRINGS_NOT_TO_BE_INCLUDED = { "Identified invalid partitioning" };
	private static final String[] STRINGS_NOT_TO_BE_INCLUDED = {};

	// for the final statistics in case of batch processsing...
	private static int totalNumberOfAnalyzedFiles = 0;
	private static int totalNumberOfMatches = 0;

	public static void main(String[] args) {
		LogfileStringAnalyzer.evaluate(LogfileStringAnalyzer.FATHER_DIRECTORY, LogfileStringAnalyzer.IS_BATCH_PROCESSING);
	}

	/**
	 * Method to start the analyzation process.
	 *
	 * @param fatherDirectory
	 *            The directory where the data to be analyzed or the folders that contain this data (in case of batch
	 *            processing) is in.
	 * @param isBatchProcessing
	 *            Indicates whether multiple folders that contain data should be processed or if it is a single folder
	 *            that is to be processed.
	 */
	public static void evaluate(String fatherDirectory, boolean isBatchProcessing) {

		// the subfolders that are to be processed
		List<File> directoriesToProcess = null;
		int nbOfDirectoriesToProcess = 1;

		if (isBatchProcessing) {
			LogfileStringAnalyzer.LOG.info("Starting batch processing ...");
			RecursiveFileSearcher rfs = new RecursiveFileSearcher();
			directoriesToProcess = rfs.searchForFilesWithMatchingName(fatherDirectory, true, "logs");
			nbOfDirectoriesToProcess = directoriesToProcess.size();
			if (LogfileStringAnalyzer.RESTRICT_NUMBER_OF_DIRECTORIES) {
				nbOfDirectoriesToProcess = Math.min(nbOfDirectoriesToProcess, LogfileStringAnalyzer.NUMBER_OF_DIRECTORIES_TO_LOAD_AT_MOST);
			}
		} else {
			LogfileStringAnalyzer.LOG.info("Starting non-batch processing ...");
		}

		// process all directories
		for (int i = 0; i < nbOfDirectoriesToProcess; i++) {
			String directory = "";
			if (isBatchProcessing) {
				directory = directoriesToProcess.get(i).getAbsolutePath();
				// check whether the directory is really a directory or just a file lying in the father directory
				if (!new File(directory).isDirectory()) {
					continue;
				}
			} else {
				directory = fatherDirectory;
			}

			LogfileStringAnalyzer.LOG.info("----------------------------------");
			LogfileStringAnalyzer.LOG.info("Analyzing directory: " + directory);

			// Start evaluation
			LogfileStringAnalyzer.analyzeLogfilesInDirectory(directory);
			LogfileStringAnalyzer.LOG.info("----------------------------------");
		}

		if (isBatchProcessing) {
			// statistical information in case of batch processing
			String tickOrFileString = LogfileStringAnalyzer.SPLIT_ANALYZATION_INTO_TICKS ? "ticks" : "files";
			LogfileStringAnalyzer.LOG.info("In total, " + LogfileStringAnalyzer.totalNumberOfMatches + " of "
					+ LogfileStringAnalyzer.totalNumberOfAnalyzedFiles + " analyzed " + tickOrFileString + " meet the search conditions.");
		}

		LogfileStringAnalyzer.LOG.info("Finished analyzation.");

		System.exit(0);
	}

	private static void analyzeLogfilesInDirectory(String directory) {

		Set<File> logfilesMeetingSearchConditions = new HashSet<File>();

		// load all .log -Files
		File[] logFiles = FileAndDirectoryHelper.loadFilteredFilesFromDirectory(directory, ".log");

		for (File logFile : logFiles) {
			// for each file: BufferedReader, readLine
			BufferedReader r = null;
			try {
				r = new BufferedReader(new FileReader(logFile));
				List<String> allLines = new ArrayList<String>();
				String line = null;
				while ((line = r.readLine()) != null) {
					if (LogfileStringAnalyzer.SPLIT_ANALYZATION_INTO_TICKS && line.contains(LogfileStringAnalyzer.TICK_SPLIT_STRING)) {
						// new tick: analyze the collected lines
						LogfileStringAnalyzer.analyzeMultipleLines(allLines, logFile, logfilesMeetingSearchConditions);
						// and reset collected lines
						allLines = new ArrayList<String>();
					} else {
						// collect this line
						allLines.add(line);
					}
				}
				// analyze the last tick or, in case no tick split is made, the whole file
				LogfileStringAnalyzer.analyzeMultipleLines(allLines, logFile, logfilesMeetingSearchConditions);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (r != null) {
					try {
						r.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
		LogfileStringAnalyzer.LOG.info("In total, " + logfilesMeetingSearchConditions.size() + " of " + logFiles.length
				+ " log files matched the search conditions.");

		// export into own file
		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new FileWriter(new File(directory + File.separator + "Logfile-Analyzation.txt")));
			w.write("Specified search strings to be included: '" + Arrays.toString(LogfileStringAnalyzer.STRINGS_TO_BE_INCLUDED) + "'");
			w.newLine();
			w.write("Specified search strings NOT to be included: '" + Arrays.toString(LogfileStringAnalyzer.STRINGS_NOT_TO_BE_INCLUDED) + "'");
			w.newLine();
			w.newLine();
			if (!logfilesMeetingSearchConditions.isEmpty()) {
				w.write("files meeting the search conditions: ");
				w.newLine();
				for (File logFile : logfilesMeetingSearchConditions) {
					w.write(logFile.getName());
					w.newLine();
				}
			} else {
				w.write("No log file in this directory found that meet the search conditions!");
				w.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Analyzes a bundle of multiple lines for the occurrence of the {@link #STRINGS_TO_BE_INCLUDED} and the
	 * non-occurrence of {@value #STRINGS_NOT_TO_BE_INCLUDED}. Thus, this method can be, e.g., called for analyzing a
	 * whole file (using all lines) or every tick (calling this method with the lines of each tick).
	 *
	 * @param lines
	 *            the lines to analyze
	 * @param logFile
	 *            the current log file
	 * @param logfilesMeetingSearchConditions
	 *            the set holding all log files that meet the search conditions
	 */
	private static void analyzeMultipleLines(List<String> lines, File logFile, Set<File> logfilesMeetingSearchConditions) {
		LogfileStringAnalyzer.totalNumberOfAnalyzedFiles++;
		Map<String, Integer> foundStringsToBeIncluded = new HashMap<String, Integer>();
		Map<String, Integer> foundStringsNotToBeIncluded = new HashMap<String, Integer>();
		// go through all lines
		for (String line : lines) {
			// no need to check the lines for the strings to be included if there are not any to check
			if (LogfileStringAnalyzer.STRINGS_TO_BE_INCLUDED.length != 0) {
				for (String s : LogfileStringAnalyzer.STRINGS_TO_BE_INCLUDED) {
					if (line.contains(s)) {
						int prevOccurrence = foundStringsToBeIncluded.get(s) == null ? 0 : foundStringsToBeIncluded.get(s);
						foundStringsToBeIncluded.put(s, prevOccurrence + 1);
					}
				}
			}
			// no need to check the lines for the strings to not be included if there are not any to check
			if (LogfileStringAnalyzer.STRINGS_NOT_TO_BE_INCLUDED.length != 0) {
				for (String s : LogfileStringAnalyzer.STRINGS_NOT_TO_BE_INCLUDED) {
					if (line.contains(s)) {
						int prevOccurrence = foundStringsNotToBeIncluded.get(s) == null ? 0 : foundStringsNotToBeIncluded.get(s);
						foundStringsNotToBeIncluded.put(s, prevOccurrence + 1);
					}
				}
			}
		}
		// if all strings to be included are included and all strings not to be included are not included: match
		// found!
		if (foundStringsToBeIncluded.size() == LogfileStringAnalyzer.STRINGS_TO_BE_INCLUDED.length && foundStringsNotToBeIncluded.size() == 0) {
			String tickString = "";
			if (LogfileStringAnalyzer.SPLIT_ANALYZATION_INTO_TICKS) {
				tickString = "One tick of ";
			}
			LogfileStringAnalyzer.LOG.info("--> " + tickString + "File " + logFile.getName()
					+ " contains all search strings to be included and does not contain the search strings not to be included!");
			for (String s : foundStringsToBeIncluded.keySet()) {
				LogfileStringAnalyzer.LOG.info("----> Occurrence of '" + s + "': " + foundStringsToBeIncluded.get(s));
			}
			for (String s : foundStringsNotToBeIncluded.keySet()) {
				LogfileStringAnalyzer.LOG.info("----> Occurrence of '" + s + "': " + foundStringsNotToBeIncluded.get(s));
			}
			logfilesMeetingSearchConditions.add(logFile);
			LogfileStringAnalyzer.totalNumberOfMatches++;
		}
	}
}
package utilities.io;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class RecursiveFileSearcher {

	private List<File> foundFiles = new ArrayList<File>();

	/**
	 * Recursively (i.e., traverses the file tree) searches for directories or files whose name is equal to the given
	 * name in the given directory.
	 *
	 * @param directoryName
	 *            the directory to start the search from
	 * @param searchForDirectory
	 *            indicates whether to search for directories or for normal files
	 * @param fileName
	 *            the desired name of the directories or normal files
	 * @return all directories or files that are below the given directory and whose name is equal to the given name
	 */
	public List<File> searchForFilesWithMatchingName(final String directoryName, final boolean searchForDirectory, final String fileName) {
		this.recursivleySearchForFilesWithMatchingName(directoryName, searchForDirectory, fileName);
		return this.foundFiles;
	}

	private void recursivleySearchForFilesWithMatchingName(final String directoryName, final boolean searchForDirectory, final String fileName) {
		// directory where to search for files
		final File directory = new File(directoryName);

		// check whether the given directory is really a directory
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("The given directory '" + directoryName + "' is not a directory!");
		}

		// we only want to read files that are a directory and have the desired name
		final FileFilter fileFilter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				boolean isCorrectType = searchForDirectory ? f.isDirectory() : f.isFile();
				boolean hasMatchingName = f.getName().toLowerCase().equals(fileName.toLowerCase());
				return isCorrectType && hasMatchingName;
			}
		};

		// get all files in directory that pass the filter
		for (File filteredFile : directory.listFiles(fileFilter)) {
			this.foundFiles.add(filteredFile);
		}

		// now get all subdirectories and continue the search in the subdirectories
		final FileFilter directoryFilter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
		};

		for (File subdirectory : directory.listFiles(directoryFilter)) {
			this.recursivleySearchForFilesWithMatchingName(subdirectory.getAbsolutePath(), searchForDirectory, fileName);
		}
	}
}
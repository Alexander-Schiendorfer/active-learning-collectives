package utilities.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Class that is responsible for directory and file operations.
 */
public class FileAndDirectoryHelper {

	/**
	 * If not yet existing, this method creates the given directory.
	 *
	 * @param directoryName
	 *            The directory to create. Should end with an "\\".
	 * @throws IOException
	 *             Thrown if directory could not be created.
	 */
	public static void createDirectory(String directoryName) throws IOException {
		File dir = new File(directoryName);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		dir = new File(directoryName);
		if (!dir.exists()) {
			throw new IOException("Could not make dir : " + dir);
		}
	}

	/**
	 * Loads at least filesCount files from the given directory if restrictFilesCount is set to true (else loads all
	 * files) with the given file extension. Note that this method does only return files and not directories. To get
	 * all directories, use the method {@link #loadSubdirectoriesFromDirectory(String)}.
	 *
	 * @param directoryName
	 *            The directory of the files to be returned.
	 * @param fileExtension
	 *            The file extension of the files to be returned, e.g, ".csv".
	 *
	 * @param restrictFilesCount
	 *            indicates whether only a defined number of files out of the directory should be analyzed
	 * @param filesCount
	 *            defines the number of files, that should be analyzed (only positive values are accepted)
	 * @return all files with the given extension
	 * @throws IllegalArgumentException
	 *             if the given directory name is not a directory
	 */
	public static File[] loadFilteredFilesFromDirectory(final String directoryName, final String fileExtension, boolean restrictFilesCount, int filesCount) {
		// directory where to search for files
		final File directory = new File(directoryName);

		// check whether the given directory is really a directory
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("The given directory '" + directoryName + "' is not a directory!");
		}

		// we only want to read files of the given file extension
		final FileFilter fileFilter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return !f.isDirectory() && f.getName().toLowerCase().endsWith(fileExtension);
			}
		};

		// get all files in directory that pass the filter
		final File[] files = directory.listFiles(fileFilter);

		// check if there are any files
		if ((files == null) || (files.length == 0)) {
			try {
				throw new FileNotFoundException("There are no files with given type '" + fileExtension + "' in the given directory '" + directoryName + "'!");
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		if (restrictFilesCount) {
			if ((filesCount > 0) && (filesCount < files.length)) {
				final File[] modifiedFileList = new File[filesCount];
				if (filesCount < files.length) {
					for (int i = 0; i < filesCount; i++) {
						modifiedFileList[i] = files[i];
					}
				}
				return modifiedFileList;
			}
		}

		return files;
	}

	/**
	 * Loads all files from the given directory with the given file extension. Note that this method does only return
	 * files and not directories. To get all directories, use the method
	 * {@link #loadSubdirectoriesFromDirectory(String)}.
	 *
	 * @param directory
	 *            The directory of the files to be returned.
	 * @param fileExtension
	 *            The file extension of the files to be returned, e.g, ".csv".
	 * @return all files with the given extension
	 * @throws IllegalArgumentException
	 *             if the given directory name is not a directory
	 */
	public static File[] loadFilteredFilesFromDirectory(String directory, final String fileExtension) {
		return FileAndDirectoryHelper.loadFilteredFilesFromDirectory(directory, fileExtension, false, -1);
	}

	/**
	 * Loads all subdirectories from the given directory.
	 *
	 * @param directoryName
	 *            The directory of the subdirectories to be returned.
	 * @return all subdirectories
	 * @throws IllegalArgumentException
	 *             if the given directory name is not a directory
	 */
	public static File[] loadSubdirectoriesFromDirectory(final String directoryName) {
		// directory where to search for files
		final File directory = new File(directoryName);

		// check whether the given directory is really a directory
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("The given directory '" + directoryName + "' is not a directory!");
		}

		// we only want to read files that are a directory
		final FileFilter fileFilter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
		};

		// get all files in directory that pass the filter
		final File[] files = directory.listFiles(fileFilter);

		// check if there are any files
		if ((files == null) || (files.length == 0)) {
			try {
				throw new FileNotFoundException("There are no subdirectories in the given directory '" + directoryName
						+ "'! Perhaps you should disable batchProcessing for the evaluation!");
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		return files;
	}

	/**
	 * Moves the given file to the given target directory.
	 *
	 * @param fileName
	 *            the file to be moved
	 * @param targetDirectoryName
	 *            the directory where to move the file
	 * @throws IOException
	 *             thrown if either the file or directory do not exist, or the file could not be moved to the target
	 *             directory
	 */
	public static void moveFileToDirectory(String fileName, String targetDirectoryName) throws IOException {
		final File fileToMove = new File(fileName);
		final File targetDir = new File(targetDirectoryName);

		if (!fileToMove.exists() || !targetDir.exists()) {
			throw new IOException("Cannot move file to directory! File " + fileToMove.getName() + " or directory " + targetDir.getAbsolutePath()
					+ " do not exist!");
		}

		// Move file to new directory
		final boolean success = fileToMove.renameTo(new File(targetDir, fileToMove.getName()));
		// File was not successfully moved
		if (!success) {
			throw new IOException("File " + fileToMove.getName() + " could not be moved to directory " + targetDir.getAbsolutePath() + "!");
		}
	}

	/**
	 * Copies a file to the given target.
	 *
	 * @param sourceFileName
	 *            file name of the file to be copied
	 * @param targetFileName
	 *            file name where to copy the file
	 */
	public static void copyFile(String sourceFileName, String targetFileName) {
		final File inputFile = new File(sourceFileName);
		final File outputFile = new File(targetFileName);

		try {
			final FileReader in = new FileReader(inputFile);
			final FileWriter out = new FileWriter(outputFile);
			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
			}
			in.close();
			out.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes a directory with all files and subdirectories.
	 *
	 * @param directoryFileName
	 *            the directory to be removed
	 *
	 * @return <code>true</code> if the directory and all of its content could be deleted
	 */
	public static boolean removeDirectory(String directoryFileName) {
		final File directory_file = new File(directoryFileName);

		if (!directory_file.exists()) {
			return true;
		}
		if (!directory_file.isDirectory()) {
			return false;
		}

		final String[] list = directory_file.list();

		// Some JVMs return null for File.list() when the
		// directory is empty.
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				final File entry = new File(directory_file, list[i]);

				if (entry.isDirectory()) {
					if (!FileAndDirectoryHelper.removeDirectory(list[i])) {
						return false;
					}
				} else {
					if (!entry.delete()) {
						return false;
					}
				}
			}
		}

		return directory_file.delete();
	}

	/**
	 * Removes a list of files.
	 *
	 * @param files
	 *            the list of file names that are to be deleted
	 */
	public static void removeFiles(List<String> files) {
		for (String file : files) {
			FileAndDirectoryHelper.removeFile(file);
		}
	}

	/**
	 * Removes a file.
	 *
	 * @param file
	 *            the file name that is to be deleted
	 */
	public static void removeFile(String file) {
		final File directory_file = new File(file);
		directory_file.delete();
	}
}
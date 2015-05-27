package utilities.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Class that is responsible for exporting data to a CSV file.
 */
public class CSVExporter {
	/**
	 * The {@link CSVExporter} responsible for generating CSV files.
	 */
	private static CSVWriter writer = null;

	/**
	 * Exports the given data to a CSV file with the given file name.
	 * 
	 * @param fileName
	 *            The filename (without the ending <code>.csv</code>)
	 * @param csvRows
	 *            The rows of the CSV that should be exported. Each element of the list is a row in the CSV.
	 */
	public static synchronized void exportDataToCSV(String fileName, List<String[]> csvRows) {
		// create CSVWriter
		try {
			// use same file as before (overwrite old one)
			writer = new CSVWriter(new FileWriter(fileName + ".csv"), ';');
		} catch (IOException e) {
			e.printStackTrace();
		}

		// write data
		writer.writeAll(csvRows);

		// close writer
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		writer = null;
	}
}
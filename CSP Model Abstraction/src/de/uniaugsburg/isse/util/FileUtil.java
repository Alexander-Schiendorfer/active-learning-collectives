package de.uniaugsburg.isse.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import de.uniaugsburg.isse.abstraction.InOutPair;

/**
 * Provides really basic support for writing csv files
 * 
 * @author alexander
 *
 */
public class FileUtil {

	public static File writeSampledPoints(Collection<InOutPair> sampledPoints) {
		File f = null;
		try {
			f = File.createTempFile("iopairs", "samp");
			double[] doubleArray = new double[sampledPoints.size() * 2];
			int i = 0;
			for (InOutPair ioPair : sampledPoints) {
				doubleArray[i] = ioPair.getInput();
				doubleArray[i + 1] = ioPair.getOutput();
				i += 2;
			}
			writeCsv(doubleArray, 2, f);
			return f;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Takes a double array which needs to have doubleArray.length = n X dim entries and writes it as n lines with dim
	 * numbers each line separated by a comma and terminated by end line
	 * 
	 * @param doubleArray
	 * @param dim
	 * @param fileName
	 */
	public static void writeCsv(double[] doubleArray, int dim, File file) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			StringBuilder lineBuilder = new StringBuilder();
			for (int i = 0; i < doubleArray.length; i += dim) {
				for (int d = 0; d < dim; ++d) {
					lineBuilder.append(doubleArray[i + d]);
					if (d + 1 < dim)
						lineBuilder.append(",");
				}
				fw.write(lineBuilder.toString() + "\n");
				lineBuilder = new StringBuilder();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public static File writeDoubleList(List<Double> doubleList) {
		File f = null;
		try {
			f = File.createTempFile("complete_input", "samp");
			double[] doubleArray = new double[doubleList.size()];
			int i = 0;
			for (double val : doubleList) {
				doubleArray[i] = val;
				++i;
			}
			writeCsv(doubleArray, 1, f);
			return f;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

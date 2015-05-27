package de.uniaugsburg.isse.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Stores interesting features of an optimization execution
 * 
 * @author alexander
 *
 */
public class AlgorithmFeatures {

	Map<String, List<String>> columns;

	public AlgorithmFeatures() {
		columns = new HashMap<String, List<String>>();
	}

	public void addFeature(String key, String value) {
		List<String> column = null;
		if (columns.containsKey(key)) {
			column = columns.get(key);
		} else {
			column = new LinkedList<String>();
			columns.put(key, column);
		}

		column.add(value);
	}

	public String writeCsv() {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> colHeaders = new ArrayList<String>(columns.keySet());
		boolean first = true;
		int maxElements = Integer.MIN_VALUE;

		for (String colHeader : colHeaders) {
			if (first)
				first = false;
			else
				sb.append(";");
			sb.append(colHeader);
			maxElements = Math.max(maxElements, columns.get(colHeader).size());
		}
		sb.append("\n");

		// now for the actual lines
		for (int line = 0; line < maxElements; ++line) {
			first = true;
			for (String colHeader : colHeaders) {
				if (first) {
					first = false;
				} else {
					sb.append(";");

				}
				List<String> column = columns.get(colHeader);

				if (line < column.size())
					sb.append(column.get(line));
			}
			sb.append("\n");

		}
		return sb.toString();
	}
}

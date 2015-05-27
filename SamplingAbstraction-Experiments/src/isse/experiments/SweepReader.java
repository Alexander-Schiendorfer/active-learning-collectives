package isse.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Reads a parameter sweep file and provides appropriate tools
 * 
 * @author alexander
 *
 */
public class SweepReader {

	public static class SweepDimension {

		private String propertyLong;
		private String propertyShort;
		private Collection<String> values;

		public SweepDimension(String propertyLong, String propertyShort, String values) {
			this.propertyLong = propertyLong;
			this.propertyShort = propertyShort;
			StringTokenizer tok = new StringTokenizer(values, " ");
			this.values = new LinkedList<String>();
			while (tok.hasMoreTokens()) {
				this.values.add(tok.nextToken());
			}
		}

		public String getPropertyShort() {
			return propertyShort;
		}

		public void setPropertyShort(String propertyShort) {
			this.propertyShort = propertyShort;
		}

		public String getPropertyLong() {
			return propertyLong;
		}

		public void setPropertyLong(String propertyLong) {
			this.propertyLong = propertyLong;
		}

		public Collection<String> getValues() {
			return values;
		}

		public void setValues(Collection<String> values) {
			this.values = values;
		}
	}

	public Collection<SweepConfig> read(String sweepFileName) {
		Scanner sc = null;

		List<SweepDimension> dimensions = new ArrayList<SweepReader.SweepDimension>();
		try {
			sc = new Scanner(new File(sweepFileName));
			String line = null;
			while (sc.hasNextLine()) {
				line = sc.nextLine();
				StringTokenizer tokenizer = new StringTokenizer(line, ";");
				String propertyLong = tokenizer.nextToken();
				String propertyShort = tokenizer.nextToken();
				String values = tokenizer.nextToken();
				dimensions.add(new SweepDimension(propertyLong, propertyShort, values));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (sc != null)
				sc.close();
		}

		Map<String, String> propertyDeltaMap = new HashMap<String, String>();
		Collection<SweepConfig> propertyDeltas = new LinkedList<SweepConfig>();
		fillRecursively(dimensions, propertyDeltas, propertyDeltaMap, 0, "");
		return propertyDeltas;
	}

	private void fillRecursively(List<SweepDimension> dimensions, Collection<SweepConfig> propertyDeltas, Map<String, String> propertyDeltaMap, int index,
			String symbolicName) {
		if (index >= dimensions.size()) {
			Map<String, String> mapCopy = new HashMap<String, String>(propertyDeltaMap);
			SweepConfig nextConfig = new SweepConfig(symbolicName, mapCopy);
			propertyDeltas.add(nextConfig);
		} else {
			// pick property for index
			SweepDimension nextDimension = dimensions.get(index);

			// go through values
			for (String value : nextDimension.getValues()) {
				propertyDeltaMap.put(nextDimension.propertyLong, value);
				fillRecursively(dimensions, propertyDeltas, propertyDeltaMap, index + 1, symbolicName + nextDimension.getPropertyShort() + "_" + value + "_");
			}
		}
	}
}

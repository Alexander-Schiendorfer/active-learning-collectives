package utilities;

import java.util.List;

/**
 * Helper class to convert {@link List}s of wrapper classes such as {@link Number}s to arrays of primitive types such as
 * doubles.
 */
public class ListToArrayConverter {

	/**
	 * Private constructor.
	 */
	private ListToArrayConverter() {
		// prevent instantiation
	}

	/**
	 * Converts a {@link List} of {@link Number}s into an array of primitive doubles.
	 *
	 * @param numberList
	 * @return
	 */
	public static double[] convertToArray(List<? extends Number> numberList) {
		double[] returnArray = new double[numberList.size()];

		int i = 0;
		for (Number n : numberList) {
			returnArray[i++] = n.doubleValue();
		}

		return returnArray;
	}
}
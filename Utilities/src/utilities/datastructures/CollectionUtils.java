package utilities.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Holds additional methods for {@link Collection}s.
 * 
 * @author Gerrit
 * 
 */
public class CollectionUtils {

	/**
	 * Transposes a given matrix represented as a list of lists.
	 * 
	 * @param <T>
	 *            the type of matrix elements
	 * @param matrix
	 *            the matrix to be transposed
	 * @return the transposed matrix
	 */
	public static <T> List<List<T>> transpose(List<List<T>> matrix) {
		List<List<T>> transposedMatrix = new ArrayList<List<T>>();

		int nbOfColumns = -1;
		int nbOfRows = -1;

		// Check if the given list of lists represents a proper matrix, i.e., each row has to feature an equal number of
		// columns
		if (matrix != null && !matrix.isEmpty()) {
			nbOfRows = matrix.size();

			for (List<T> row : matrix) {
				if (nbOfColumns == -1)
					nbOfColumns = row.size();
				else if (nbOfColumns != row.size())
					throw new IllegalArgumentException("The given list of lists is not a proper matrix.");
			}
		}

		// transpose the matrix
		for (int i = 0; i < nbOfColumns; i++) {
			List<T> newRow = new ArrayList<T>(nbOfRows);

			for (List<T> row : matrix) {
				newRow.add(row.get(i));
			}

			transposedMatrix.add(newRow);
		}

		return transposedMatrix;
	}
}

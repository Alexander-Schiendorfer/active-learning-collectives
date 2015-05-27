package utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Provides the possibility to calculate all combinations, i.e., the power set (or power multiset), of a given list of
 * elements in an <b>iterative</b> manner. Note that the {@link CombinationProvider} is able to handle multisets.
 *
 * @author Gerrit
 *
 * @param <T>
 */
public class CombinationProvider<T> {

	/**
	 * Used for testing purposes.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		Date d_start = new Date();
		long startMillis = System.currentTimeMillis();

		System.out.println("Starting at: " + d_start);

		int nbOfElements = 20;
		ArrayList<Integer> elements = new ArrayList<Integer>(nbOfElements);
		for (int i = 1; i <= nbOfElements; i++) {
			elements.add(i);
		}

		CombinationProvider<Integer> combProv = new CombinationProvider<Integer>(elements);
		for (int i = 1; i <= Math.pow(2, nbOfElements) + 1; i++) {
			// System.out.println("Further comb. available: " + combProv.isFurtherCombinationAvailable());
			// System.out.println("Further non-empty comb. available: " +
			// combProv.isFurtherNonEmptyCombinationAvailable());
			List<Integer> nextCombination = combProv.determineNextCombination();
			System.out.println(i + ": " + nextCombination);
		}

		System.out.println("---");

		Date d_end = new Date();
		long endMillis = System.currentTimeMillis();
		long duration = endMillis - startMillis;
		System.out.println("Finished at: " + d_end + "; duration " + duration + "ms");
	}

	/**
	 * Elements the power set should be calculated for.
	 */
	private final List<T> elements;

	/**
	 * The size of {@link #elements}
	 */
	private final int nbOfElements;

	/**
	 * The last calculated combination.
	 */
	private final List<T> combination;

	/**
	 * The total number of combinations.
	 */
	private final long totalNbOfCombinations;

	/**
	 * The number of already calculated combinations.
	 */
	private long nbOfCalculatedCombinations = 0;

	/* *******************************
	 * DATA FOR STATE RECONSTRUCTION *
	 *********************************/

	/**
	 * Used for reconstruction: The indices of elements in {@link #elements} contained in the combination to
	 * reconstruct.
	 */
	private ArrayList<Integer> reconstructStateIndices = new ArrayList<Integer>();

	/**
	 * Used for reconstruction: Indicates the number of elements in the combination to reconstruct.
	 */
	private int reconstructionDepth = -1;

	/**
	 * Used for reconstruction: Indicates whether the state of the combination was reconstructed.
	 */
	private boolean isReconstructionFinished = true;

	/**
	 * The indices of elements in {@link #elements} contained in the current {@link #combination}.
	 */
	private ArrayList<Integer> currentStateIndices = new ArrayList<Integer>();

	/**
	 * Indicates the number of elements in the current {@link #combination}.
	 */
	private int currentDepth = -1;

	/**
	 * Indicates whether {@link #determineNextCombination()} has been called at least once.
	 */
	private boolean isFirstCall = true;

	/**
	 * Creates a new {@link CombinationProvider} for the given elements.
	 *
	 * @param elements
	 *
	 */
	public CombinationProvider(Collection<T> elements) {
		this.elements = new ArrayList<T>(elements);
		this.nbOfElements = this.elements.size();

		this.combination = new ArrayList<T>(); // TODO: suitable size for initialization?
		this.totalNbOfCombinations = (long) Math.pow(2, this.nbOfElements);
	}

	/**
	 *
	 * @return {@link #nbOfElements}
	 */
	public int getNbOfElements() {
		return this.nbOfElements;
	}

	/**
	 *
	 * @return {@link #totalNbOfCombinations}
	 */
	public long getTotalNbOfCombinations() {
		return this.totalNbOfCombinations;
	}

	/**
	 *
	 * @return {@link #nbOfCalculatedCombinations}
	 */
	public long getNbOfCalculatedCombinations() {
		return this.nbOfCalculatedCombinations;
	}

	/**
	 *
	 * @return <code>true</code> if there is a further combination available.
	 */
	public boolean isFurtherCombinationAvailable() {
		return this.nbOfCalculatedCombinations < this.totalNbOfCombinations;
	}

	/**
	 *
	 * @return <code>true</code> if there is a further non-empty combination available.
	 */
	public boolean isFurtherNonEmptyCombinationAvailable() {
		return this.nbOfCalculatedCombinations + 1 < this.totalNbOfCombinations;
	}

	/**
	 * Determines a further combination. The last calculated combination is empty.
	 *
	 * @return The combination or <code>null</code> in case all combinations were calculated.
	 */
	public List<T> determineNextCombination() {
		if (!this.isFirstCall) {
			// all combinations were calculated
			if (!this.isFurtherCombinationAvailable())
				return null;

			// initialize data for state reconstruction
			this.isReconstructionFinished = false;

			ArrayList<Integer> tmpList = this.reconstructStateIndices;
			tmpList.clear();

			this.reconstructStateIndices = this.currentStateIndices;
			this.reconstructionDepth = this.currentDepth;

			// reset information about the new combination
			this.currentStateIndices = tmpList;
			this.currentDepth = -1;
		} else {
			// in case of the first call, we do not have to reconstruct anything
			this.isFirstCall = false;
		}

		this.reconstructAndDetermineNewCombination(0, 0);
		this.nbOfCalculatedCombinations++;

		return new ArrayList<T>(this.combination);
	}

	/**
	 * Determines a further combination. The last calculated combination is empty. Must not be called after all
	 * combinations were calculated.
	 *
	 * @param startIndex
	 *            the index to start with when iterating over elements in {@link #elements}
	 * @param depth
	 *            indicates the number of elements in the combination, i.e., the depth of the recursion
	 * @return
	 */
	private boolean reconstructAndDetermineNewCombination(int startIndex, int depth) {
		if (startIndex <= this.nbOfElements - 1) {
			boolean firstRound = true;
			boolean enforceEnlargeCombination = false;

			while (firstRound || enforceEnlargeCombination) {
				firstRound = false;

				// determine the correct index to start with, depending on whether a reconstruction is performed or not
				int myStartIndex;
				if (!this.isReconstructionFinished) {
					myStartIndex = this.reconstructStateIndices.get(depth);
				} else {
					myStartIndex = startIndex;
				}

				for (int index = myStartIndex; index < this.nbOfElements; index++) {
					T element = this.elements.get(index);

					// reconstruct combination (reconstruction depth not reached)
					if (!this.isReconstructionFinished && depth < this.reconstructionDepth) {
						// reconstruct state (element is already contained in the combination but not in the state)
						this.addElementToCombinationState(depth, index);

						// greater depth has to be reconstructed
						boolean nextCombinationDetermined = this.reconstructAndDetermineNewCombination(index + 1, depth + 1);

						if (nextCombinationDetermined) {
							// new combination found
							return true;
						} else {
							// no new combination found: try new combination without this element
							this.removeLastElementFromCombination();
							this.removeLastElementFromCombinationState();

							// determine how to proceed
							if (index == this.nbOfElements - 1) {
								// if there are no further elements available on this depth, return
								return false;
							} else {
								// if we already tried all combination with elements of higher depths, enlarge the
								// combination
								enforceEnlargeCombination = true;
							}
						}
					}
					// reconstruct combination (reconstruction depth reached)
					else if (!this.isReconstructionFinished) {
						// remove the last element from the combination -- otherwise we would calculate the same
						// combination again.
						this.removeLastElementFromCombination();

						// last element of state to reconstruct will not be reconstructed
						this.isReconstructionFinished = true;

						// determine the way the new combination should be created
						if (index == this.nbOfElements - 1) {
							// if we already tried all combination with elements in this depth, enlarge the combination
							enforceEnlargeCombination = true;
						} else {
							// do nothing: perform next iteration where next element will be added
						}
					}
					// reconstruction already finished: enlarge combination by adding an element of greater depth
					else if (enforceEnlargeCombination) {
						enforceEnlargeCombination = false;

						// this element has to be contained in the combination
						this.addElementToCombination(index, element);
						this.addElementToCombinationState(depth, index);

						// extend the combination
						boolean nextCombinationDetermined = this.reconstructAndDetermineNewCombination(index + 1, depth + 1);

						// if the combination could not be extended, remove the last added element
						if (nextCombinationDetermined == false) {
							this.removeLastElementFromCombination();
							this.removeLastElementFromCombinationState();
						}

						return nextCombinationDetermined;
					}
					// reconstruction already finished: add a new element without enlarging the combination
					else {
						// determine new combination by adding this element
						this.addElementToCombination(index, element);
						this.addElementToCombinationState(depth, index);

						// we found a new combination
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Removes the last element of {@link #combination}.
	 */
	private void removeLastElementFromCombination() {
		this.combination.remove(this.combination.size() - 1);
	}

	/**
	 * Adds the specified element to {@link #combination} at the specified index.
	 *
	 * @param index
	 * @param element
	 */
	private void addElementToCombination(int index, T element) {
		if (this.combination.size() - 1 >= index) {
			this.combination.set(index, element);
		} else {
			this.combination.add(element);
		}
	}

	/**
	 * Removes the last element from {@link #currentStateIndices} and decrements {@link #currentDepth}.
	 */
	private void removeLastElementFromCombinationState() {
		this.currentStateIndices.remove(this.currentStateIndices.size() - 1);
		this.currentDepth--;
	}

	/**
	 * Adds the index for the specified depth in {@link #currentStateIndices} and updates {@link #currentDepth}.
	 *
	 * @param depth
	 * @param index
	 */
	private void addElementToCombinationState(int depth, int index) {
		if (this.currentStateIndices.size() - 1 >= depth) {
			this.currentStateIndices.set(depth, index);
		} else {
			this.currentStateIndices.add(index);
		}
		this.currentDepth = depth;
	}
}

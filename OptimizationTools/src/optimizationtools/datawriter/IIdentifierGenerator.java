package optimizationtools.datawriter;

/**
 * Generates object IDs to be used in a CPLEX data file.
 * 
 * @author lehnepat
 * 
 * @param <T>
 *            the type of object for which this {@code IdenfierGenerator} creates IDs
 */
public interface IIdentifierGenerator<T> {
	/**
	 * <p>
	 * Generate a "unique" identifier for <b>obj</b> -- optionally incorporating the value of <b>runningCounter</b> --
	 * for a certain definition of "unique".
	 * </p>
	 * 
	 * <p>
	 * The notion of uniqueness required of the generated identifiers is quite relative, and also depends on the
	 * concrete CPLEX model for which you are created a data file.
	 * </p>
	 * 
	 * <p>
	 * The ID must be unique at least per every generation run, for more information see
	 * {@link CPLEXDataWriter#generateItemIDs(java.util.Collection, IdentifierGenerator)}.
	 * </p>
	 * 
	 * @see CPLEXDataWriter#generateItemIDs(java.util.Collection, IdentifierGenerator)
	 * 
	 * @param obj
	 *            the object for which to generate an ID
	 * @param runningCounter
	 *            the value of a running counter incremented by the generating loop; you can assume that per generation
	 *            run, this value is unique <i>per object occurrence</i> in the collection over which the generating
	 *            loop iterates (this means that incorporating the value of this counter into the ID should usually
	 *            satisfy the required relative uniqueness of the generated ID)
	 * @return a "unique" identifier for <b>obj</b>
	 */
	public String generateIdentifier(final T obj, final int runningCounter);
}

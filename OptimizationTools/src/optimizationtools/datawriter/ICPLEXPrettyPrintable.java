package optimizationtools.datawriter;

/**
 * Interface to mark all CPLEX data file elements which can be pretty printed, such as tuple arrays or other indexed
 * arrays.
 * 
 * @author lehnepat
 * 
 */
public interface ICPLEXPrettyPrintable {

	/**
	 * <p>
	 * Get the content of this CPLEX element, in a pretty-printed way.
	 * </p>
	 * 
	 * <p>
	 * The first line of the content must be un-indented (its indentation is handled by the containing entity, as it may
	 * be preceded by a name or index).
	 * </p>
	 * 
	 * <p>
	 * All further lines must be indented by at least <b>startIndent</b> spaces, and all nested indentations must
	 * increase by <b>indentBy</b> spaces.
	 * </p>
	 * 
	 * @param startIndent
	 * @param indentBy
	 * @return
	 */
	// public String getPrettyContent(int startIndent, final int indentBy);
}

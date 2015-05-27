package optimizationtools.datawriter;

/**
 * The interface to mark all elements that may appear in a CPLEX data file.
 * 
 * @author lehnepat
 * 
 */
public interface ICPLEXElement {
	public String getName();

	public void setName(String name);

	public String getContent(boolean prettyPrintingEnabled, final int startIndent, final int indentBy);

	@Override
	public String toString();
}

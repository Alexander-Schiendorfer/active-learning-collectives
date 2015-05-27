package optimizationtools.datawriter;

/**
 * An abstract class that implements the common base functionality of all regular CPLEX data file elements: the
 * element's name attribute (with getter and setter), and forwarding {@link #toString()} to {@link #getContent()}.
 * 
 * @author lehnepat
 * 
 */
public abstract class CPLEXAbstractElement implements ICPLEXElement {

	protected String name;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getContent(false, 0, 0);
	}

}

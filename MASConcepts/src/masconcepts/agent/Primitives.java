package masconcepts.agent;

/**
 * This class specifies the interface of an {@link AgentComponent}, i.e., the messages an {@link AgentComponent} is able
 * to receive and process.
 * 
 * @author Oliver
 * 
 */
public abstract class Primitives {

	protected final String prefix;

	public Primitives(Class<?> theClass) {
		this.prefix = theClass.getName() + ".";
	}
}

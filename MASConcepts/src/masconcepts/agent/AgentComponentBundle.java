package masconcepts.agent;

import java.util.Set;

/**
 * An {@link AgentComponent} that consists of a set of subordinate {@link AgentComponent}s. Subordinate
 * {@link AgentComponent}s should not be registered with their {@link AgentComponent#getOwner()}. Instead, the bundle
 * should implement the interface, i.e., it should include and register all necessary primitives and delegate work to
 * subordinate {@link AgentComponent}s as needed.
 * 
 * @author Gerrit
 * 
 * @param <T>
 *            the type of the {@link AgentComponent}'s owner
 */
public abstract class AgentComponentBundle<T extends IMessageHandlingAgent> extends AgentComponent<T> {

	/**
	 * Instantiates an {@link AgentComponentBundle}.
	 * 
	 * @see AgentComponent
	 * 
	 * @param owner
	 *            the underlying {@link IMessageHandlingAgent} that owns and executes the created {@link AgentComponent}
	 */
	public AgentComponentBundle(T owner) {
		super(owner);
	}

	/**
	 * Gets the set of subordinate {@link AgentComponent}s.
	 * 
	 * @return the set of subordinate {@link AgentComponent}s.
	 */
	protected abstract Set<AgentComponent<?>> getSubordinateAgentComponents();
}

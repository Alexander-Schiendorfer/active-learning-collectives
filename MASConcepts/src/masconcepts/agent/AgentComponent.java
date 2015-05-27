package masconcepts.agent;

import masconcepts.primitives.components.AgentComponentPrimitives;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import utilities.ClassHierarchyStringBuilder;

/**
 * An <code>AgentComponent</code> represents a kind of generic capability or functionality of an agent. An
 * {@link IMessageHandlingAgent} can incorporate the functionality defined by the corresponding
 * <code>AgentComponent</code> by instantiating it. AgentComponents also extend their owners with the possibility to
 * receive AgentComponent specific Messages. The Classes holding the Primitives are registered automatically if they are
 * named like the AgentCompontent + "Primitives". The Primitives of an AgentComponents super classes are registered as
 * well, so the AgentComponents PrimitiveClasses should only contain Primitives that are not already defined in one of
 * their super classes. PrimitiveClasses have to extend the {@link Primitives} class for automatic registration.
 *
 * @author Gerrit
 *
 * @param <T>
 *            the type of the {@link AgentComponent}'s owner
 */
public abstract class AgentComponent<T extends IMessageHandlingAgent> implements IMessageRecipient {

	/**
	 * For logging purposes.
	 */
	private Logger LOG;

	/**
	 * Indicates whether or not {@link #LOG} is initialized with a logger name containing the owner's agent identifier.
	 * This flag is used for reducing unnecessary computation overhead.
	 */
	private boolean loggerInitializedWithAgentIdentifier = false;

	/**
	 * The underlying {@link IMessageHandlingAgent} that owns and executes this {@link AgentComponent}.
	 */
	protected T owner;

	/**
	 * Creates an {@link AgentComponent}. Registers Primitive classes for the {@link AgentComponent}, so the component
	 * owners methods can be called via messages. Primitive classes have to be named in the following way:
	 * {@link AgentComponent} + "Primitives". Moreover the Primitive classes have to be placed in the same package as
	 * their associated {@link AgentComponent}. Super classes that associate other PrimitiveClasses are registered
	 * automatically.
	 *
	 * @param owner
	 *            The underlying {@link IMessageHandlingAgent} that owns and executes the created {@link AgentComponent}
	 */
	public AgentComponent(T owner) {
		this(owner, true);
	}

	/**
	 * Creates an {@link AgentComponent}. Registers Primitive classes for the {@link AgentComponent} if desired, so the
	 * component owners methods can be called via messages. Primitive classes have to be named in the following way:
	 * {@link AgentComponent} + "Primitives". Moreover, the Primitive classes have to be placed in the same package as
	 * their associated {@link AgentComponent}. Super classes that associate other PrimitiveClasses are registered
	 * automatically.
	 *
	 * @param owner
	 *            The underlying {@link IMessageHandlingAgent} that owns and executes the created {@link AgentComponent}
	 * @param registerWithOwner
	 *            indicates whether the {@link AgentComponent} should be registered with its owner
	 */
	public AgentComponent(T owner, boolean registerWithOwner) {
		super();

		this.owner = owner;

		// initialize logger
		this.tryInitializeAgentComponentLoggerNameWithIdentifier();

		if (registerWithOwner) {
			MessageRecipientHelper.registerIMessageRecipientPrimitives(this, owner);
		}
	}

	/**
	 * Gets the underlying {@link IMessageHandlingAgent} that owns and executes this {@link AgentComponent}.
	 *
	 * @return The underlying {@link IMessageHandlingAgent} that owns and executes this {@link AgentComponent}.
	 */
	public T getOwner() {
		return this.owner;
	}

	/**
	 * Returns the {@link AgentComponent}'s {@link Primitives}.
	 *
	 * @return the {@link AgentComponent}'s {@link Primitives}
	 */
	public static AgentComponentPrimitives getPrimitives() {
		return null;
	}

	/**
	 * @return The {@link Logger} for this {@link AgentComponent} with a name based on the class hierarchy and, if
	 *         available, with the owner's agent identifier.
	 */
	public Logger getLogger() {
		if (this.loggerInitializedWithAgentIdentifier)
			return this.LOG;
		else
			return this.tryInitializeAgentComponentLoggerNameWithIdentifier();
	}

	/**
	 * Tries to initialize the name of the {@link Logger} for this {@link AgentComponent} with the agent identifier if
	 * available. If not, a name simply consisting of the class hierarchy is used.
	 *
	 * @return the {@link Logger} -- might, however, not be named with the agent identifier!
	 */
	private Logger tryInitializeAgentComponentLoggerNameWithIdentifier() {
		String classHierarchyString = ClassHierarchyStringBuilder.getClassHierarchyStringForClass(this.getClass(), AgentComponent.class);
		try {
			// sadly, we cannot access the AgentIdentifier class and have to split the agent identifier here
			String id = this.getOwner().getAgentIdentifier(); // -> can lead to exception

			int index = id.lastIndexOf("#");
			if (index == -1)
				throw new IllegalArgumentException();
			id = id.substring(index + 1);
			this.LOG = LogManager.getLogger(classHierarchyString + "#" + id);

			// prevent further initialization when accessing the logger
			this.loggerInitializedWithAgentIdentifier = true;
		} catch (Exception e) {
			// Exception because no (proper) agent identifier is available -- use normal logger name
			this.LOG = LogManager.getLogger(classHierarchyString);
			this.LOG.debug("Could not get (proper) agent identifier of the owner of agent component '" + this.getClass().getSimpleName()
					+ "'. Using logger without agent identifier.");
		}

		return this.LOG;
	}
}
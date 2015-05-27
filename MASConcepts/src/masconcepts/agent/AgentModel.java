package masconcepts.agent;

import java.io.Serializable;

/**
 * An {@link AgentModel} represents the believe state of an {@link IAgent}.
 *
 * @author Oliver
 *
 */
public abstract class AgentModel implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3630397054290119735L;

	/**
	 * The {@link AgentModel}'s owner.
	 */
	protected final String agentID;

	/**
	 * Creates a new {@link AgentModel}.
	 *
	 * @param agentID
	 *            the agent identifier of the associated {@link IAgent}
	 */
	public AgentModel(String agentID) {
		this.agentID = agentID;
	}

	/**
	 * @return Gets the agent identifier of this {@link AgentModel}'s owner.
	 */
	public String getAgentID() {
		return this.agentID;
	}
}
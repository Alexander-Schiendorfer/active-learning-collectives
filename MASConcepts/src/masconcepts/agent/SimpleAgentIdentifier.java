package masconcepts.agent;

import java.io.Serializable;

/**
 * A simple agent identifier holding a string representation as well as a numerical identifier.
 */
public class SimpleAgentIdentifier implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8889277057854307538L;

	/**
	 * The agent identifier represented as string.
	 */
	private final String agentIdentifier;

	/**
	 * A numerical identifier.
	 */
	private final int numericalIdentifier;

	/**
	 * Creates a new {@link SimpleAgentIdentifier} with the given arguments.
	 *
	 * @param agentIdentifier
	 *            The agent identifier represented as string.
	 * @param numericalIdentifier
	 *            A numerical identifier.
	 */
	public SimpleAgentIdentifier(String agentIdentifier, int numericalIdentifier) {
		super();
		this.agentIdentifier = agentIdentifier;
		this.numericalIdentifier = numericalIdentifier;
	}

	@Override
	public String toString() {
		return this.agentIdentifier;
	}

	/**
	 * Returns the numerical identifier.
	 *
	 * @return the numerical identifier
	 */
	public int getNumericalIdentifier() {
		return this.numericalIdentifier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.agentIdentifier == null) ? 0 : this.agentIdentifier.hashCode());
		result = prime * result + this.numericalIdentifier;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		SimpleAgentIdentifier other = (SimpleAgentIdentifier) obj;
		if (this.agentIdentifier == null) {
			if (other.agentIdentifier != null) {
				return false;
			}
		} else if (!this.agentIdentifier.equals(other.agentIdentifier)) {
			return false;
		}
		if (this.numericalIdentifier != other.numericalIdentifier) {
			return false;
		}
		return true;
	}
}
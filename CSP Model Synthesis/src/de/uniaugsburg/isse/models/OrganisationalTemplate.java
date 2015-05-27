package de.uniaugsburg.isse.models;

import java.util.Set;
import java.util.TreeSet;

/**
 * This class represents the organisational template and its contents provides (decision) variables as well as the
 * objective; in particular this class is the queryable object and not a string dummy
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class OrganisationalTemplate extends ConstraintSatisfactionProblem {
	private String objective;
	private String agentSetIdent;
	private final Set<String> plantSpecificIdentifiers;
	private final Set<String> nonPlantSpecificIdentifiers;

	public Set<String> getNonPlantSpecificIdentifiers() {
		return nonPlantSpecificIdentifiers;
	}

	public OrganisationalTemplate() {
		super();
		plantSpecificIdentifiers = new TreeSet<String>();
		nonPlantSpecificIdentifiers = new TreeSet<String>();
	}

	public String getAgentSetIdent() {
		return agentSetIdent;
	}

	public String getObjective() {
		return objective;
	}

	public void setObjective(String objective) {
		this.objective = objective;
	}

	public void setAgentSetIdent(String agentSet) {
		this.agentSetIdent = agentSet;
	}

	public Set<String> getPlantSpecificIdentifiers() {
		return plantSpecificIdentifiers;
	}

	public void calculateNonSpecificIdents() {
		for (String s : getIdentifiers().getVariables())
			if (!plantSpecificIdentifiers.contains(s))
				nonPlantSpecificIdentifiers.add(s);
	}
}

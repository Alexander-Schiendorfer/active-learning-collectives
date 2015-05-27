package de.uniaugsburg.isse.models;

import de.uniaugsburg.isse.models.data.ConstraintSet;
import de.uniaugsburg.isse.models.data.IdentifierSet;

/**
 * Common base class for IAMs and OTs
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class ConstraintSatisfactionProblem {

	public static enum ModelElements {
		IDENTS, OBJECTIVE, CONSTRAINTS
	}

	protected IdentifierSet identifiers;
	protected ConstraintSet constraints;
	protected String name;

	public ConstraintSet getConstraints() {
		return constraints;
	}

	public void setConstraints(ConstraintSet constraints) {
		this.constraints = constraints;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IdentifierSet getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(IdentifierSet identifiers) {
		this.identifiers = identifiers;
	}
}

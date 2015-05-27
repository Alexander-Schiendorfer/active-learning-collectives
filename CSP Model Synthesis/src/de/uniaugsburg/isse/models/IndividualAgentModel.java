package de.uniaugsburg.isse.models;

import java.util.List;

/**
 * Represents an individual agent model (as of now just a CSP)
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class IndividualAgentModel extends ConstraintSatisfactionProblem {
	private List<String> internalIdentifierLines; // contains actual lines such as dvar int ...
	private List<String> constraintLines; // contains actual lines of constraints
	private String sourceCode;

	public List<String> getInternalIdentifierLines() {
		return internalIdentifierLines;
	}

	public void setInternalIdentifierLines(List<String> internalIdentifierLines) {
		this.internalIdentifierLines = internalIdentifierLines;
	}

	public List<String> getConstraintLines() {
		return constraintLines;
	}

	public void setConstraintLines(List<String> constraintLines) {
		this.constraintLines = constraintLines;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}
}

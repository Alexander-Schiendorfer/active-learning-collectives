package de.uniaugsburg.isse.models.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class containing all variables and decision variables and generally identifiers used for both organisational
 * template (OT) and individual agent models (IAM)
 * 
 * @author alexander
 * 
 */
public class IdentifierSet {
	private List<String> variables;
	private List<String> decisionVariables;
	private List<String> decisionExpressions;
	private List<String> interfaceIdentifiers; // contains ALL identifiers in the interface

	public IdentifierSet() {
		this.variables = new ArrayList<String>();
		this.decisionVariables = new ArrayList<String>();
		this.decisionExpressions = new ArrayList<String>();
		this.interfaceIdentifiers = new ArrayList<String>();
	}

	public List<String> getVariables() {
		return variables;
	}

	public void setVariables(List<String> variables) {
		this.variables = variables;
	}

	public List<String> getDecisionVariables() {
		return decisionVariables;
	}

	public void setDecisionVariables(List<String> decisionVariables) {
		this.decisionVariables = decisionVariables;
	}

	public List<String> getDecisionExpressions() {
		return decisionExpressions;
	}

	public void setDecisionExpressions(List<String> decisionExpressions) {
		this.decisionExpressions = decisionExpressions;
	}

	public List<String> getAllIdentifiers() {
		return interfaceIdentifiers;
	}

	public void setInterfaceIdentifiers(List<String> interfaceIdentifiers) {
		this.interfaceIdentifiers = interfaceIdentifiers;
	}

}

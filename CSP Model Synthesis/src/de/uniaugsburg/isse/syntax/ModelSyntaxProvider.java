package de.uniaugsburg.isse.syntax;

import java.util.Collection;

import de.uniaugsburg.isse.models.data.ConstraintCrawler;
import de.uniaugsburg.isse.models.data.IdentifierCrawler;

/**
 * Defines the replacement for local and interface variables
 * 
 * @author Alexander Schiendorfer
 * 
 */
public interface ModelSyntaxProvider {
	/**
	 * In the case of an interface variable, the OT may assume a set of identifiers of all agents and may index them,
	 * thus this could translate to production |-> production["a"]
	 * 
	 * @param interfaceVariable
	 *            the identifier of the OT to be replaced
	 * @param agentIdentifier
	 *            the agent's individual identifier
	 * @return a syntactic replacement
	 */
	String getSyntaxForInterfaceVariable(String interfaceVariable, String agentIdentifier);

	/**
	 * A local variable is replaced differently, a "new" syntactically unique variable must be introduced to the OT
	 * 
	 * @param interfaceVariable
	 *            the identifier of the OT to be replaced
	 * @param agentIdentifier
	 *            the agent's individual identifier
	 * @return a syntactic replacement
	 */
	String getSyntaxForLocalVariable(String localVariable, String agentIdentifier);

	/**
	 * Returns a language specific crawler for identifiers
	 * 
	 * @return
	 */
	IdentifierCrawler getIdentifierCrawler();

	/**
	 * Returns a language specific crawler for constraints
	 * 
	 * @return
	 */
	ConstraintCrawler getConstraintCrawler();

	String getMinimizeKeyword();

	String getMaximizeKeyword();

	String getConstraintsKeyword();

	String getConstraintsEndKeyword();

	String replaceAgentsPlaceHolder(String newLine, Collection<String> keySet);

	boolean isAgentSetDefinition(String agentSetIdent, String newLine);

	String getConstraintRelationshipsBegin();

	String getConstraintRelationshipsEnd();

	String getMoreImportantSym();

	String getLineEnd();

	/**
	 * Extracts an interface variable's value (sort of constant) such as float P_max = 50; should return the string "50"
	 * 
	 * @param valueLine
	 * @return
	 */
	String extractValue(String valueLine);

	/**
	 * Replaces a place holder for conrete interface variables' values
	 * 
	 * @param variablesSet
	 * @return
	 */
	String replaceVariablePlaceHolder(String line, Collection<String> individualValues);

}

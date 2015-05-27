package de.uniaugsburg.isse.syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import de.uniaugsburg.isse.models.ModelSynthesisException;
import de.uniaugsburg.isse.models.data.ConstraintCrawler;
import de.uniaugsburg.isse.models.data.IdentifierCrawler;
import de.uniaugsburg.isse.models.data.naive.CplexConstraintCrawler;
import de.uniaugsburg.isse.models.data.naive.CplexIdentifierCrawler;

/**
 * Provides model syntax replacemtents for CPLEX OPL
 * 
 * @author alexander
 * 
 */
public class CplexModelSyntaxProvider implements ModelSyntaxProvider {

	@Override
	public String getSyntaxForInterfaceVariable(String interfaceVariable, String agentIdentifier) {
		return interfaceVariable + "[\"" + agentIdentifier + "\"]";
	}

	@Override
	public String getSyntaxForLocalVariable(String localVariable, String agentIdentifier) {
		return localVariable + "_" + agentIdentifier;
	}

	@Override
	public IdentifierCrawler getIdentifierCrawler() {
		return new CplexIdentifierCrawler();
	}

	@Override
	public ConstraintCrawler getConstraintCrawler() {
		return new CplexConstraintCrawler();
	}

	@Override
	public String getMinimizeKeyword() {
		return "minimize";
	}

	@Override
	public String getMaximizeKeyword() {
		return "maximize";
	}

	@Override
	public String getConstraintsKeyword() {
		return "subject to {";
	}

	@Override
	public String getConstraintsEndKeyword() {
		return "};";
	}

	@Override
	public String replaceAgentsPlaceHolder(String newLine, Collection<String> keySet) {
		return newLine.replace("...", toCplexSet(toStringSet(keySet)));
	}

	private Collection<String> toStringSet(Collection<String> strings) {
		ArrayList<String> stringSet = new ArrayList<String>(strings.size());
		for (String s : strings) {
			stringSet.add("\"" + s + "\"");
		}
		return stringSet;
	}

	private String toCplexSet(Collection<String> keySet) {
		StringBuilder arrBuilder = new StringBuilder("{");
		boolean first = true;
		for (String key : keySet) {
			if (first)
				first = false;
			else
				arrBuilder.append(", ");
			arrBuilder.append(key);
		}
		arrBuilder.append("}");
		return arrBuilder.toString();
	}

	private String toCplexArray(Collection<String> keySet) {
		StringBuilder arrBuilder = new StringBuilder("[");
		boolean first = true;
		for (String key : keySet) {
			if (first)
				first = false;
			else
				arrBuilder.append(", ");
			arrBuilder.append(key);
		}
		arrBuilder.append("]");
		return arrBuilder.toString();
	}

	@Override
	public boolean isAgentSetDefinition(String agentSetIdent, String newLine) {
		if (newLine != null && agentSetIdent != null && newLine.contains(agentSetIdent)) {
			StringTokenizer tok = new StringTokenizer(newLine);
			tok.nextToken(); // has to be type
			if (!tok.hasMoreElements())
				return false;
			String nextTok = tok.nextToken();

			if (agentSetIdent.equals(nextTok))
				return true;
		}
		return false;
	}

	@Override
	public String getConstraintRelationshipsBegin() {
		return "/* SOFT-CONSTRAINTS";
	}

	@Override
	public String getConstraintRelationshipsEnd() {
		return "End SOFT-CONSTRAINTS */";
	}

	@Override
	public String getMoreImportantSym() {
		return ">>";
	}

	@Override
	public String getLineEnd() {
		return ";";
	}

	@Override
	public String extractValue(String valueLine) {
		// TODO use regex to make this more beautiful
		// line consists of something like type name = value;
		StringTokenizer tok = new StringTokenizer(valueLine.trim(), " ;");
		String s = tok.nextToken(); // should be type
		if (!tok.hasMoreTokens())
			throw new ModelSynthesisException("Malformed variable assignment: " + valueLine);
		tok.nextToken(); // should be name
		if (!tok.hasMoreTokens())
			throw new ModelSynthesisException("Malformed variable assignment: " + valueLine);
		tok.nextToken(); // should be equals sign
		if (!tok.hasMoreTokens())
			throw new ModelSynthesisException("Malformed variable assignment: " + valueLine);

		s = tok.nextToken(); // this should be the value

		return s;
	}

	@Override
	public String replaceVariablePlaceHolder(String line, Collection<String> individualValues) {
		return line.replace("...", toCplexArray(individualValues));
	}
}

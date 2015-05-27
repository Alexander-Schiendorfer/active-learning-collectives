package de.uniaugsburg.isse.models.data.naive;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import de.uniaugsburg.isse.models.IdentType;
import de.uniaugsburg.isse.models.data.IdentifierCrawler;
import de.uniaugsburg.isse.models.data.IdentifierSet;
import de.uniaugsburg.isse.models.data.LineSplitter;

public class CplexIdentifierCrawler extends IdentifierCrawler {

	private final List<String> basicTypes;
	private final List<String> tuples;

	public CplexIdentifierCrawler() {
		basicTypes = Arrays.asList("string", "int", "float", "boolean", "range", "stepFunction");
		tuples = new ArrayList<String>();
	}

	@Override
	public IdentifierSet readIdentifiers(List<String> lines) {
		// read all idents by going through file line by line
		StringTokenizer tok = null;
		String newIdent = null;
		IdentType identType = IdentType.VAR;
		IdentifierSet identifierSet = new IdentifierSet();
		String tokenizeString = " [;,";
		boolean inTupleDef = false; // for now, ignore tuple definitions as identifiers

		for (String newLine : lines) {
			if (containsType(newLine) && !inTupleDef) {
				identType = IdentType.VAR;
				tok = new StringTokenizer(newLine, tokenizeString);
				String help = tok.nextToken();
				if (help.contains("dvar")) {
					tok.nextToken();
					identType = IdentType.DEC_VAR;
				}

				if (help.contains("dexpr")) {
					tok.nextToken();
					identType = IdentType.DEC_EXPR;
				}

				// otherwise help contained the type
				newIdent = tok.nextToken(); // consider arrays as well
				switch (identType) {
				case VAR:
					identifierSet.getVariables().add(newIdent);
					break;
				case DEC_VAR:
					identifierSet.getDecisionVariables().add(newIdent);
					break;
				case DEC_EXPR:
					identifierSet.getDecisionExpressions().add(newIdent);
				default:
					break;
				}
				notifyListeners(identType, newLine, newIdent);
				notifyListeners(IdentType.ANY, newLine, newIdent);
				identifierSet.getAllIdentifiers().add(newIdent);
			}
			if (newLine.contains("tuple")) { // assume tuple xyz { has to be the first occurrence on a line
				tok = new StringTokenizer(newLine, tokenizeString);
				tok.nextToken(); // should be tuple
				String tupleType = tok.nextToken();
				tuples.add(tupleType);
				inTupleDef = true;
			}

			if (newLine.contains("};")) // terminating symbol for tuples
				inTupleDef = false;
		}
		return identifierSet;
	}

	/**
	 * Utility to check if a line contains either a basic type or a composite one to test for identifiers
	 * 
	 * @param newLine
	 * @return
	 */
	private boolean containsType(String newLine) {
		for (String type : basicTypes)
			if (newLine.contains(type))
				return true;

		for (String type : tuples)
			if (newLine.contains(type))
				return true;
		return false;
	}

	@Override
	public IdentifierSet readIdentifiers(File oplFileReference) {
		LineSplitter ls = new LineSplitter();
		List<String> lines = ls.readLines(oplFileReference);
		return readIdentifiers(lines);
	}
}

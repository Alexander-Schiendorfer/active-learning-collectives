package de.uniaugsburg.isse.models.data.naive;

import java.io.File;
import java.util.List;

import de.uniaugsburg.isse.models.IdentType;
import de.uniaugsburg.isse.models.ModelSynthesisException;
import de.uniaugsburg.isse.models.OrganisationalTemplate;
import de.uniaugsburg.isse.models.data.ConstraintCrawler;
import de.uniaugsburg.isse.models.data.ConstraintSet;
import de.uniaugsburg.isse.models.data.IdentifierSet;
import de.uniaugsburg.isse.models.data.LineListener;
import de.uniaugsburg.isse.models.data.LineSplitter;
import de.uniaugsburg.isse.models.data.OrganisationalTemplateFactory;

/**
 * This very basic OPL parser assumes that variables are always defined on an own line with int xyz or dexpr/dvar int
 * xyz being the first keywords; set types have to be written as {xyz} with no blanks (then read as one symbol); a tuple
 * has to be ended by "};". "subject to" similarly needs to end with "};" Also minimization functions and constraints
 * should be written in one line, Keyword for agent set has to be on an own line, following a line of the keyword
 * 
 * <pre>
 * AgentSet
 * plants
 * </pre>
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class NaiveOPLOrganisationalTemplateFactory implements OrganisationalTemplateFactory {

	private File oplFileReference;
	private final String agentSetKeyword = "AgentSet";

	/**
	 * Makes sure to distinguish between interface variables for each power plant (such as production) and global ones
	 * (TIMERANGE)
	 * 
	 * @author alexander
	 * 
	 */
	private static class InterfaceVariableDecider implements LineListener {
		private final OrganisationalTemplate ot;

		public InterfaceVariableDecider(OrganisationalTemplate ot) {
			this.ot = ot;
		}

		@Override
		public void receiveLine(String line, String ident) {
			if (line.contains(ot.getAgentSetIdent())) { // then mark interface variable for all
				ot.getPlantSpecificIdentifiers().add(ident);
			}
		}

	}

	public NaiveOPLOrganisationalTemplateFactory(File oplFileReference) {
		this.oplFileReference = oplFileReference;

	}

	@Override
	public OrganisationalTemplate create() {
		OrganisationalTemplate ot = new OrganisationalTemplate();

		LineSplitter ls = new LineSplitter();
		List<String> lines = ls.readLines(oplFileReference);

		// find identifier for set of agents
		boolean foundKeyword = false;
		for (String line : lines) {
			if (foundKeyword) {
				String agentSet = line.trim();
				ot.setAgentSetIdent(agentSet);
				foundKeyword = false;
			}
			if (line.contains(agentSetKeyword)) {
				foundKeyword = true;
			}
		}

		CplexIdentifierCrawler ic = new CplexIdentifierCrawler();
		// ot might not contain a specifier for agent set
		if (ot.getAgentSetIdent() == null) {
			throw new ModelSynthesisException("You forgot to specify an agent set identifier in the OT file " + oplFileReference.getAbsolutePath());
		}
		ic.registerLineListener(IdentType.ANY, new InterfaceVariableDecider(ot));
		IdentifierSet identifiers = ic.readIdentifiers(oplFileReference);
		ot.setIdentifiers(identifiers);
		ot.calculateNonSpecificIdents();

		// find minimization function
		for (String line : lines) {
			if (line.contains("maximize") || line.contains("minimize")) {
				ot.setObjective(line);
			}
		}

		ConstraintCrawler cc = new CplexConstraintCrawler(lines);

		// find constraints
		ConstraintSet cs = cc.readConstraintSet();
		ot.setConstraints(cs);

		return ot;
	}

	@Override
	public OrganisationalTemplate create(String organisationalTemplateFile) {
		this.oplFileReference = new File(organisationalTemplateFile);
		return create();
	}

}

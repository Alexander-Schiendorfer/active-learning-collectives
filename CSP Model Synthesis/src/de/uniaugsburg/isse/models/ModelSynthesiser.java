package de.uniaugsburg.isse.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.StringTokenizer;

import de.uniaugsburg.isse.models.ConstraintSatisfactionProblem.ModelElements;
import de.uniaugsburg.isse.models.data.ConstraintCrawler;
import de.uniaugsburg.isse.models.data.ConstraintSet;
import de.uniaugsburg.isse.models.data.IdentifierCrawler;
import de.uniaugsburg.isse.models.data.IdentifierSet;
import de.uniaugsburg.isse.models.data.LineListener;
import de.uniaugsburg.isse.models.data.LineSplitter;
import de.uniaugsburg.isse.models.data.OrganisationalTemplateFactory;
import de.uniaugsburg.isse.syntax.ModelSyntaxProvider;

/**
 * Initiates the actual synthesis process
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class ModelSynthesiser {

	private static class IndividualConstraintExtractor implements LineListener {

		private final List<String> localConstraintsList;

		public IndividualConstraintExtractor(List<String> localConstraintsList) {
			this.localConstraintsList = localConstraintsList;
		}

		@Override
		public void receiveLine(String line, String ident) {
			if (ident == null)
				this.localConstraintsList.add(line);
		}

	}

	private OrganisationalTemplateFactory factory;

	public OrganisationalTemplateFactory getFactory() {
		return factory;
	}

	public void setFactory(OrganisationalTemplateFactory factory) {
		this.factory = factory;
	}

	private ModelSyntaxProvider modelSyntaxProvider;
	private Map<String, Map<String, String>> initialValues; // for all decision variables of individual agents

	public Map<String, Map<String, String>> getInitialValues() {
		return initialValues;
	}

	public void setInitialValues(Map<String, Map<String, String>> initialValues) {
		this.initialValues = initialValues;
	}

	public ModelSyntaxProvider getModelSyntaxProvider() {
		return modelSyntaxProvider;
	}

	public void setModelSyntaxProvider(ModelSyntaxProvider modelSyntaxProvider) {
		this.modelSyntaxProvider = modelSyntaxProvider;
	}

	private static class IndividualVariableExtractor implements LineListener {

		private final List<String> individualVariables;
		private final Map<String, Map<String, String>> valueOfVariable;
		private final OrganisationalTemplate orgTemp;
		private final String agentIdent;

		public IndividualVariableExtractor(List<String> individualVariables, OrganisationalTemplate ot, Map<String, Map<String, String>> valueOfVariable,
				String key) {
			this.individualVariables = individualVariables;
			this.orgTemp = ot;
			this.valueOfVariable = valueOfVariable;
			this.agentIdent = key;
		}

		@Override
		public void receiveLine(String line, String ident) {
			if (ident != null) {
				Collection<String> decExprVars = new ArrayList<String>(orgTemp.getIdentifiers().getAllIdentifiers().size());
				decExprVars.addAll(orgTemp.getIdentifiers().getDecisionExpressions());
				decExprVars.addAll(orgTemp.getIdentifiers().getDecisionVariables());
				for (String decExprVar : decExprVars) {
					if (ident.equals(decExprVar)) {
						return; // if I found an interface decision variable, I don't need to care
					}
				}

				for (String nonPlantSpec : orgTemp.getNonPlantSpecificIdentifiers()) {
					if (ident.equals(nonPlantSpec))
						return; // don't need non plant specific interface variable from IAM definition
				}

				for (String localVar : orgTemp.getPlantSpecificIdentifiers()) {
					if (ident.equals(localVar)) {
						// just store lines for now
						if (valueOfVariable.get(ident) == null)
							valueOfVariable.put(ident, new HashMap<String, String>());
						Map<String, String> identVariables = valueOfVariable.get(ident);
						identVariables.put(agentIdent, line);
						return;
					}
				}

				// test if it is a plain variable f
				individualVariables.add(line);
			}
		}

	}

	public String synthesise(String organisationalTemplateFile, Collection<String> individualAgentModels) {
		if (modelSyntaxProvider == null)
			throw new ModelSynthesisException("No model syntax provider specified");
		if (organisationalTemplateFile == null)
			throw new ModelSynthesisException("No organisational template factory specified");

		OrganisationalTemplate ot = factory.create(organisationalTemplateFile);

		// now preprocess all individual agent model files
		Map<String, String> individualAgentLineLists = preprocess(ot, individualAgentModels);

		List<String> variableLines = new LinkedList<String>();
		List<String> constraintLines = new LinkedList<String>();
		LineSplitter ls = new LineSplitter();
		List<String> otLists = ls.readLines(new File(organisationalTemplateFile));

		String objectiveKeyword = (ot.getObjective().contains(modelSyntaxProvider.getMinimizeKeyword())) ? modelSyntaxProvider.getMinimizeKeyword()
				: modelSyntaxProvider.getMaximizeKeyword();

		String constraintsKeyword = modelSyntaxProvider.getConstraintsKeyword();

		// then extract identifiers etc from the preprocessed file

		ConstraintSatisfactionProblem.ModelElements phase = ModelElements.IDENTS;
		boolean terminated = false;

		for (String newLine : otLists) {
			if (newLine.contains(objectiveKeyword)) { // identifier part is now over
				phase = ModelElements.OBJECTIVE;
			}
			if (newLine.contains(constraintsKeyword)) {
				phase = ModelElements.CONSTRAINTS;
				continue; // has to "stutter" for one step
			}
			if (newLine.contains(modelSyntaxProvider.getConstraintsEndKeyword())) {
				terminated = true;
				break;
			}

			if (phase == ModelElements.IDENTS) {
				if (modelSyntaxProvider.isAgentSetDefinition(ot.getAgentSetIdent(), newLine)) {
					newLine = modelSyntaxProvider.replaceAgentsPlaceHolder(newLine, individualAgentLineLists.keySet());
				}

				variableLines.add(newLine);
			} else if (phase == ModelElements.CONSTRAINTS) {
				constraintLines.add(newLine);
			}
		}
		if (!terminated) {
			throw new ModelSynthesisException("Did you forget a " + modelSyntaxProvider.getConstraintsEndKeyword() + " to close a subject to block?");
		}

		List<String> localVariablesList = new LinkedList<String>();
		Map<String, Map<String, String>> interfaceVariableMap = new HashMap<String, Map<String, String>>();

		List<String> localConstraintsList = new LinkedList<String>();
		List<String> localConstraintRelationships = new LinkedList<String>();
		List<String> localHeadConstraints = new LinkedList<String>();

		for (Entry<String, String> entry : individualAgentLineLists.entrySet()) {
			LineSplitter splitter = new LineSplitter();
			List<String> iamLines = splitter.readLines(entry.getValue());
			IdentifierCrawler ic = modelSyntaxProvider.getIdentifierCrawler();
			ic.registerLineListener(IdentType.ANY, new IndividualVariableExtractor(localVariablesList, ot, interfaceVariableMap, entry.getKey()));
			ic.readIdentifiers(iamLines);

			// now constraints as well
			ConstraintCrawler cc = modelSyntaxProvider.getConstraintCrawler();
			cc.registerLineListener(new IndividualConstraintExtractor(localConstraintsList));
			ConstraintSet cs = cc.readConstraintSet(iamLines);
			localHeadConstraints.addAll(cs.getHeadConstraints());
			localConstraintRelationships.addAll(cs.getConstraintRelationships());
		}

		// get values of interface variables
		Map<String, List<String>> ifVarValueMap = new HashMap<String, List<String>>();
		for (Entry<String, Map<String, String>> ifVarEntry : interfaceVariableMap.entrySet()) {
			List<String> individualValues = new ArrayList<String>(individualAgentModels.size());
			Map<String, String> agentToValueMap = ifVarEntry.getValue();
			for (String agent : individualAgentLineLists.keySet()) { // same order as agent set
				String valueLine = agentToValueMap.get(agent);
				if (valueLine == null)
					throw new ModelSynthesisException("You missed an interface variable! " + ifVarEntry.getKey() + " in  model " + agent);
				String extractedValue = modelSyntaxProvider.extractValue(valueLine);
				individualValues.add(extractedValue);
			}
			ifVarValueMap.put(ifVarEntry.getKey(), individualValues);
		}

		// now finally combine
		StringBuilder synthesisedModelBuilder = new StringBuilder();
		// first all OT vars
		for (String otVar : variableLines) {
			for (String var : ot.getPlantSpecificIdentifiers()) {
				if (var.equals(ot.getAgentSetIdent()))
					continue;
				if (otVar.contains(var) && ifVarValueMap.containsKey(var)) {
					StringTokenizer tok = new StringTokenizer(otVar);
					String first = tok.nextToken();
					if (tok.hasMoreTokens()) {
						first = tok.nextToken();
						if (first.contains(var)) { // here we search for replacement of ...;
							List<String> variablesSet = ifVarValueMap.get(var);
							otVar = modelSyntaxProvider.replaceVariablePlaceHolder(otVar, variablesSet);
						}
					}
				}
			}
			synthesisedModelBuilder.append(otVar + "\n");
		}
		// then all local vars
		for (String locVar : localVariablesList) {
			synthesisedModelBuilder.append(locVar + "\n");
		}

		// then the objective
		synthesisedModelBuilder.append(ot.getObjective() + "\n");

		// constraints section
		synthesisedModelBuilder.append(modelSyntaxProvider.getConstraintsKeyword() + "\n");

		if (initialValues != null) { // some initial values have been set
			for (Entry<String, Map<String, String>> agentInitialValues : initialValues.entrySet()) {
				String agentIdent = getAgentNameFromFile(agentInitialValues.getKey());
				for (Entry<String, String> keyValPair : agentInitialValues.getValue().entrySet()) {
					String variableName = keyValPair.getKey();
					String value = keyValPair.getValue();
					String newVariableName = null;
					if (ot.getIdentifiers().getDecisionVariables().contains(variableName)) {
						newVariableName = modelSyntaxProvider.getSyntaxForInterfaceVariable(variableName, agentIdent);
					} else
						newVariableName = modelSyntaxProvider.getSyntaxForLocalVariable(variableName, agentIdent);

					String initLine = newVariableName + "[0] == " + value + ";";
					synthesisedModelBuilder.append(initLine + "\n");
				}
			}
		}
		for (String constraintLine : constraintLines) {
			synthesisedModelBuilder.append(constraintLine + "\n");
		}

		for (String localConstraintLine : localConstraintsList) {
			synthesisedModelBuilder.append(localConstraintLine + "\n");
		}
		synthesisedModelBuilder.append(modelSyntaxProvider.getConstraintsEndKeyword() + "\n");

		synthesisedModelBuilder.append(modelSyntaxProvider.getConstraintRelationshipsBegin() + "\n");
		for (String constraintRelationshipLine : ot.getConstraints().getConstraintRelationships()) {
			synthesisedModelBuilder.append(constraintRelationshipLine + "\n");
		}

		// add all organsiatorial constraint relationships to be more important than individual ones

		for (String orgSoftConstraint : ot.getConstraints().getSoftConstraints()) {
			for (String localHeadConstraint : localHeadConstraints) {
				synthesisedModelBuilder.append(orgSoftConstraint + " " + modelSyntaxProvider.getMoreImportantSym() + " " + localHeadConstraint
						+ modelSyntaxProvider.getLineEnd() + "\n");
			}
		}
		for (String constraintRelationshipLine : localConstraintRelationships) {
			synthesisedModelBuilder.append(constraintRelationshipLine + "\n");
		}
		synthesisedModelBuilder.append(modelSyntaxProvider.getConstraintRelationshipsEnd() + "\n");
		return synthesisedModelBuilder.toString();
	}

	private Map<String, String> preprocess(OrganisationalTemplate ot, Collection<String> individualAgentModels) {
		HashMap<String, String> individualAgentLineLists = new HashMap<String, String>();

		for (String individualAgentFile : individualAgentModels) {
			// read line by line and do replacements
			Scanner sc = null;
			String agentIdent = getAgentNameFromFile(individualAgentFile);
			try {
				// first read file for all local identifiers
				IdentifierCrawler ic = modelSyntaxProvider.getIdentifierCrawler();
				IdentifierSet is = ic.readIdentifiers(new File(individualAgentFile));

				// get constraints as well
				ConstraintCrawler cc = modelSyntaxProvider.getConstraintCrawler();
				ConstraintSet cs = cc.readConstraintSet(new File(individualAgentFile));

				// then read file for direct replacement
				sc = new Scanner(new File(individualAgentFile));
				StringBuilder sb = new StringBuilder();
				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					sb.append(line + "\n");
				}
				sc.close();
				String modelSource = sb.toString();

				// replace all interface variables (if plant specific!)
				for (String interfaceIdent : ot.getPlantSpecificIdentifiers()) {
					String replacement = modelSyntaxProvider.getSyntaxForInterfaceVariable(interfaceIdent, agentIdent);
					modelSource = modelSource.replaceAll(interfaceIdent, replacement);
				}
				// replace all local variables of this IAM
				for (String localIdent : is.getAllIdentifiers()) {
					if (!containsLocal(localIdent, ot.getIdentifiers().getAllIdentifiers())) {
						String replacement = modelSyntaxProvider.getSyntaxForLocalVariable(localIdent, agentIdent);
						modelSource = modelSource.replaceAll(localIdent, replacement);
					}
				}

				// replace all constraints by the same identifiers
				for (String localConstraint : cs.getConstraints()) {
					String replacement = modelSyntaxProvider.getSyntaxForLocalVariable(localConstraint, agentIdent);
					modelSource = modelSource.replaceAll(localConstraint, replacement);
				}

				individualAgentLineLists.put(agentIdent, modelSource);
			} catch (FileNotFoundException e) {
				throw new ModelSynthesisException("Unable to read individual agent model " + individualAgentFile, e);
			} finally {
				if (sc != null)
					sc.close();
			}
		}
		return individualAgentLineLists;
	}

	private String getAgentNameFromFile(String individualAgentFile) {
		if (individualAgentFile.contains(".")) {
			return individualAgentFile.substring(0, individualAgentFile.indexOf(".")).replaceAll("/", "_");
		} else
			return individualAgentFile.replaceAll("/", "");
	}

	private boolean containsLocal(String localIdent, List<String> allIdentifiers) {
		for (String interfaceVariable : allIdentifiers) {
			if (localIdent.equals(interfaceVariable))
				return true;
		}
		return false;
	}
}

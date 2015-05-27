package de.uniaugsburg.isse.models.data.naive;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uniaugsburg.isse.models.ModelSynthesisException;
import de.uniaugsburg.isse.models.data.ConstraintCrawler;
import de.uniaugsburg.isse.models.data.ConstraintSet;
import de.uniaugsburg.isse.models.data.LineSplitter;

public class CplexConstraintCrawler extends ConstraintCrawler {

	private List<String> lines;
	private final String SOFT_CONSTRAINT_DELIMITER = "SOFT-CONSTRAINTS";
	private File file;

	public CplexConstraintCrawler(List<String> lines) {
		this.lines = lines;
	}

	public CplexConstraintCrawler(File oplReference) {
		LineSplitter ls = new LineSplitter();
		this.lines = ls.readLines(oplReference);
	}

	public CplexConstraintCrawler() {
	}

	@Override
	public ConstraintSet readConstraintSet() {
		if (this.lines == null)
			throw new ModelSynthesisException("Lines not defined in constraint crawler");

		ConstraintSet cs = new ConstraintSet();
		Pattern constraintNamePattern = Pattern.compile("\\s*(\\w*)\\s*:.*");
		List<String> constraintNames = cs.getConstraints();
		String constraintsStart = "subject ";
		String constraintsEnd = "};";

		boolean constraintsActivated = false;
		boolean constraintsEnded = false;
		for (String line : lines) {
			if (line.contains(constraintsStart)) {
				constraintsActivated = true;
				continue;
			}

			if (constraintsActivated) {
				if (line.contains(constraintsEnd)) {
					constraintsEnded = true;
					break;
				}

				Matcher constraintNameMatcher = constraintNamePattern.matcher(line);
				if (constraintNameMatcher.find()) {
					String constraintName = constraintNameMatcher.group(1);
					constraintNames.add(constraintName);
					notifyListeners(line, constraintName);
				}
				// notify of the line anyways, without a constraint name (so it's clear that it's only a line)
				notifyListeners(line, null);
			}
		}
		if (constraintsActivated && !constraintsEnded) {
			throw new ModelSynthesisException("You started a constraints section but did not close it. Did you maybe forget a " + constraintsEnd + "? File "
					+ file);
		}

		// find soft constraints
		List<String> constraintRelationships = cs.getConstraintRelationships();
		boolean softMode = false;
		for (String line : lines) {
			if (softMode && !line.contains(SOFT_CONSTRAINT_DELIMITER)) {
				line = line.replaceAll(";", "");
				constraintRelationships.add(line);

				StringTokenizer tok = new StringTokenizer(line);
				while (tok.hasMoreTokens()) {
					String s = tok.nextToken();
					if (!s.contains(">>")) {
						String toAdd = s.trim();
						if (!cs.getSoftConstraints().contains(toAdd))
							cs.getSoftConstraints().add(toAdd);
					}
				}
			}
			if (line.contains(SOFT_CONSTRAINT_DELIMITER)) {
				softMode = !softMode;
			}
		}
		cs.parseConstraintRelationships();
		return cs;
	}

	@Override
	public ConstraintSet readConstraintSet(File oplFileReference) {
		LineSplitter ls = new LineSplitter();
		this.file = oplFileReference;
		this.lines = ls.readLines(oplFileReference);
		return readConstraintSet();
	}

	@Override
	public ConstraintSet readConstraintSet(List<String> lines) {
		this.lines = lines;
		return readConstraintSet();
	}

}

package de.uniaugsburg.isse.models.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class ConstraintSet {

	protected List<String> constraints;
	protected List<String> softConstraints;
	protected List<String> constraintRelationships;
	private boolean[][] adjacencyMatrix; // for the constraint relationship graph
	private Map<String, Integer> keyToIndexMap;
	private HashMap<Integer, String> indexToKeyMap;

	public ConstraintSet() {
		constraints = new ArrayList<String>();
		softConstraints = new ArrayList<String>();
		constraintRelationships = new ArrayList<String>();
	}

	public List<String> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<String> constraints) {
		this.constraints = constraints;
	}

	public List<String> getSoftConstraints() {
		return softConstraints;
	}

	public void setSoftConstraints(List<String> softConstraints) {
		this.softConstraints = softConstraints;
	}

	public List<String> getConstraintRelationships() {
		return constraintRelationships;
	}

	public void setConstraintRelationships(List<String> constraintRelationships) {
		this.constraintRelationships = constraintRelationships;
	}

	public void parseConstraintRelationships() {
		adjacencyMatrix = new boolean[softConstraints.size()][softConstraints.size()];
		for (int i = 0; i < adjacencyMatrix.length; ++i) {
			for (int j = 0; j < adjacencyMatrix[0].length; ++j)
				adjacencyMatrix[i][j] = false;
		}
		keyToIndexMap = new HashMap<String, Integer>();
		indexToKeyMap = new HashMap<Integer, String>();
		int i = 0;
		for (String sc : softConstraints) {
			keyToIndexMap.put(sc, i);
			indexToKeyMap.put(i, sc);
			++i;
		}

		// now insert edges from constraint relationships such that am[i][j] indicates that i >> j
		for (String crLine : constraintRelationships) {
			if (crLine.contains(">>")) {
				crLine = crLine.replaceAll(">>", ">");
				StringTokenizer tok = new StringTokenizer(crLine, ">");
				String first = tok.nextToken().trim();
				String second = tok.nextToken().trim();

				adjacencyMatrix[keyToIndexMap.get(first)][keyToIndexMap.get(second)] = true;
			}
		}
	}

	/**
	 * Returns the head constraints, i.e. those that are not dominated by others
	 * 
	 * <pre>
	 * \{ c \in C_s \mid \not \exists c' \in C_s : c' >_R c \}
	 * </pre>
	 * 
	 * @return empty list if no constraints are available (or cyclic graph)
	 */
	public List<String> getHeadConstraints() {
		List<String> headConstraints = new LinkedList<String>();
		int nConstraints = softConstraints.size();
		if (adjacencyMatrix != null) {
			for (int i = 0; i < nConstraints; ++i) {
				boolean dominated = false;
				for (int j = 0; j < nConstraints; ++j) {
					if (adjacencyMatrix[j][i]) {
						dominated = true;
						break;
					}
				}
				if (!dominated) {
					headConstraints.add(indexToKeyMap.get(i));
				}
			}
		}
		return headConstraints;
	}

	public int getPredecessorCount(String softConstraint) {
		int index = keyToIndexMap.get(softConstraint);
		int predCount = 1;
		for (int j = 0; j < softConstraints.size(); ++j) {
			if (adjacencyMatrix[index][j])
				++predCount;
		}
		return predCount;
	}
}

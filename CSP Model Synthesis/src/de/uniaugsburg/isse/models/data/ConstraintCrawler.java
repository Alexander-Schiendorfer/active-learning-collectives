package de.uniaugsburg.isse.models.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Looks for constraints, soft constraints and constraint relationships
 * 
 * @author alexander
 * 
 */
public abstract class ConstraintCrawler {
	private Collection<LineListener> lineListeners;

	public abstract ConstraintSet readConstraintSet();

	public abstract ConstraintSet readConstraintSet(File oplFileReference);

	public abstract ConstraintSet readConstraintSet(List<String> lines);

	public void registerLineListener(LineListener lineListener) {
		if (lineListeners == null)
			lineListeners = new ArrayList<LineListener>();
		lineListeners.add(lineListener);
	}

	protected void notifyListeners(String line, String constraintName) {
		if (lineListeners != null) {
			for (LineListener listener : lineListeners) {
				listener.receiveLine(line, constraintName);
			}
		}
	}
}

package de.uniaugsburg.isse.models.data;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uniaugsburg.isse.models.IdentType;

/**
 * Utility class to crawl a file for identifiers
 * 
 * @author alexander
 * 
 */
public abstract class IdentifierCrawler {

	protected Map<IdentType, Collection<LineListener>> lineListeners;

	public abstract IdentifierSet readIdentifiers(File oplFileReference);

	public abstract IdentifierSet readIdentifiers(List<String> lines);

	/**
	 * publish request to listen for specific lines while identifiers are parsed
	 * 
	 * @param type
	 * @param listener
	 */
	public void registerLineListener(IdentType type, LineListener listener) {
		if (lineListeners == null) {
			lineListeners = new HashMap<IdentType, Collection<LineListener>>();
		}

		if (!lineListeners.containsKey(type)) {
			lineListeners.put(type, new LinkedList<LineListener>());
		}

		lineListeners.get(type).add(listener);
	}

	/**
	 * Internal method to notify all line listeners
	 * 
	 * @param type
	 * @param line
	 */
	protected void notifyListeners(IdentType type, String line, String ident) {
		if (lineListeners == null)
			return;
		if (!lineListeners.containsKey(type))
			return;
		for (LineListener ll : lineListeners.get(type)) {
			ll.receiveLine(line, ident);
		}
	}
}

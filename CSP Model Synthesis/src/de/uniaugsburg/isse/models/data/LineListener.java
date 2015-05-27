package de.uniaugsburg.isse.models.data;

/**
 * Interface for some code that wants to listen to each line of a kind in a parsing process (e.g. when the identifier
 * crawler works)
 * 
 * @author alexander
 * 
 */
public interface LineListener {
	/**
	 * Notifies if an ident (or constraint) has been found and provides the respective line ident may be null
	 * 
	 * @param line
	 */
	void receiveLine(String line, String ident);
}

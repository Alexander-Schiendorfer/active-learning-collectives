package de.uniaugsburg.isse.abstraction;

/**
 * Exception to denote e.g. that a sampling point could not be found
 * 
 * @author alexander
 *
 */
public class SamplingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5027374318763878705L;

	public SamplingException(String exceptionMessage) {
		super(exceptionMessage);
	}
}

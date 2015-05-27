package de.uniaugsburg.isse.models;

import java.io.FileNotFoundException;

public class ModelSynthesisException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8133705759063734016L;

	public ModelSynthesisException(String string, FileNotFoundException e) {
		super(string, e);
	}

	public ModelSynthesisException(String string) {
		super(string);
	}
}

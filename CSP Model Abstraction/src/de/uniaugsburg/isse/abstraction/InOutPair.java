package de.uniaugsburg.isse.abstraction;

import java.io.Serializable;

/**
 * Represents a sampled input/output pair for a functional relationship such as power to costs or power to maximal next
 * power
 * 
 * @author alexander
 *
 */
public class InOutPair implements Comparable<InOutPair>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4751398232846480581L;

	public double input;
	public double output;

	public InOutPair(double input, double output) {
		super();
		this.input = input;
		this.output = output;
	}

	public double getOutput() {
		return output;
	}

	public void setOutput(double output) {
		this.output = output;
	}

	public double getInput() {
		return input;
	}

	public void setInput(double input) {
		this.input = input;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj instanceof InOutPair) {
			InOutPair other = (InOutPair) obj;
			return other.input == input && other.output == output;
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return new Double(input).hashCode() * new Double(output).hashCode();
	}

	@Override
	public int compareTo(InOutPair o) {
		return Double.compare(input, o.input);
	}

	@Override
	public String toString() {
		return "[" + input + " -> " + output + "]";
	}

}

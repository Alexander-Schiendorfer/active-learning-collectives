package utilities.parameters.sweepfilesgenerator;

/**
 * Class that maps a parameter name to one single value.
 */
public class Parameter {

	public final String name;
	public final Object value;

	public Parameter(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String toString() {
		return "ParameterNameToValue [name=" + this.name + ", value=" + this.value + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		Parameter other = (Parameter) obj;
		if (this.name == null) {
			if (other.name != null)
				return false;
		} else if (!this.name.equals(other.name))
			return false;
		if (this.value == null) {
			if (other.value != null)
				return false;
		} else if (!this.value.equals(other.value))
			return false;
		return true;
	}
}
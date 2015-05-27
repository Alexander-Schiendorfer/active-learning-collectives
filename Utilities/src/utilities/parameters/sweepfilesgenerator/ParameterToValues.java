package utilities.parameters.sweepfilesgenerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that maps a parameter name to multiple values.
 */
public class ParameterToValues {

	public final String name;
	public final Object[] values;

	public ParameterToValues(String name, Object[] values) {
		this.name = name;
		this.values = values;
	}

	public String getName() {
		return this.name;
	}

	public Object[] getValues() {
		return this.values;
	}

	public Set<Parameter> generateParameterValues() {
		Set<Parameter> parameterNameToValueSet = new HashSet<Parameter>();
		for (Object parameterValue : this.values) {
			parameterNameToValueSet.add(new Parameter(this.name, parameterValue));
		}

		return parameterNameToValueSet;
	}

	@Override
	public String toString() {
		return "ParameterNameToValues [name=" + this.name + ", values=" + Arrays.toString(this.values) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + Arrays.hashCode(this.values);
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
		ParameterToValues other = (ParameterToValues) obj;
		if (this.name == null) {
			if (other.name != null)
				return false;
		} else if (!this.name.equals(other.name))
			return false;
		if (!Arrays.equals(this.values, other.values))
			return false;
		return true;
	}
}

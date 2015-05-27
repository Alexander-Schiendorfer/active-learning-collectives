package isse.experiments;

import java.util.Map;

/**
 * Represents one particular property delta consisting of all updated settings and a short symbolic name
 * 
 * @author alexander
 *
 */
public class SweepConfig {

	private String symbolicName;
	private Map<String, String> propertyDelta;

	public SweepConfig(String symbolicName, Map<String, String> propertyDelta) {
		super();
		this.symbolicName = symbolicName;
		this.propertyDelta = propertyDelta;
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	public Map<String, String> getPropertyDelta() {
		return propertyDelta;
	}

	public void setPropertyDelta(Map<String, String> propertyDelta) {
		this.propertyDelta = propertyDelta;
	}

}

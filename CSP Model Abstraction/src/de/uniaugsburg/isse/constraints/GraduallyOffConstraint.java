package de.uniaugsburg.isse.constraints;

import de.uniaugsburg.isse.powerplants.PowerPlantData;

public class GraduallyOffConstraint extends PlantConstraint {

	private final double P_min;

	// no preferences when maximizing
	public GraduallyOffConstraint(PowerPlantData pd) {
		this.P_min = pd.getPowerBoundaries().min;
	}

	/**
	 * Has to return the on status therefore inverting the condition
	 */
	@Override
	public boolean minimizeBool() {
		double p_next = getPlant().getPower().min;
		return (p_next > P_min);
	}

}

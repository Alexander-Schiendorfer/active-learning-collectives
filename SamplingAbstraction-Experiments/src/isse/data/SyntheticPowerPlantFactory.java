package isse.data;

import de.uniaugsburg.isse.powerplants.PowerPlantData;

public interface SyntheticPowerPlantFactory {
	/**
	 * Creates a new instance of a certain type of power plant
	 * @return
	 */
	PowerPlantData createPowerPlant ();
	
	/**
	 * Returns the number of power plants of the respective type
	 * @return
	 */
	int getNumber();

	/**
	 * Specifies if plants can be switched off
	 * @param disconnectable
	 */
	void setDisconnectable(boolean disconnectable);

	public abstract boolean isDisconnectable();

	public abstract void setMinutesPerTimestep(int minutesPerTimestep);

	public abstract int getMinutesPerTimestep();

	public abstract void setDetailedInertia(boolean detailedInertia);

	public abstract boolean isDetailedInertia();
}

package de.uniaugsburg.isse.powerplants;

import java.util.List;

/**
 * Just a plain power plant source, returns a collection of power plant data;
 * this has to be handed to the experiment prior to execution initialized
 * with the proper number of agents
 * @author alexander
 *
 */
public interface PowerPlantSource {
	List<PowerPlantData> drawPowerPlants();
}

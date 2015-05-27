package isse.data;

import java.io.File;

import de.uniaugsburg.isse.constraints.GraduallyOffConstraint;
import de.uniaugsburg.isse.constraints.StartWithMinConstraint;
import de.uniaugsburg.isse.constraints.StopTimeConstraint;
import de.uniaugsburg.isse.powerplants.PowerPlantData;

/**
 * Can add thermal constraints such as minimal running / standing times if necessary
 * 
 * @author alexander
 *
 */
public class BiofuelPowerplantFactory extends DefaultPowerPlantFactory {

	public BiofuelPowerplantFactory(PowerPlantType type, File distributionFile, double minimumOutputPercent, double slope, double meanPricePerKwh,
			double sdPricePerKwh) {
		super(type, distributionFile, minimumOutputPercent, slope, meanPricePerKwh, sdPricePerKwh);
	}

	@Override
	public PowerPlantData createPowerPlant() {
		PowerPlantData defaultPlant = super.createPowerPlant();
		if (isDetailedInertia()) {
			defaultPlant.addConstraint(new GraduallyOffConstraint(defaultPlant));
			defaultPlant.addConstraint(new StopTimeConstraint(defaultPlant));
			defaultPlant.addConstraint(new StartWithMinConstraint(defaultPlant));
		}
		return defaultPlant;
	}

}

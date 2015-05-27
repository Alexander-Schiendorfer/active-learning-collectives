package isse.data;

import java.io.File;

import utilities.RoundingHelper;
import de.uniaugsburg.isse.abstraction.types.Interval;

public class HydroPowerPlantFactory extends DefaultPowerPlantFactory {

	public HydroPowerPlantFactory(File distributionFile,
			double minimumOutputPercent, double slope, double meanPricePerKwh,
			double sdPricePerKwh) {
		super(PowerPlantType.HYDRO, distributionFile, minimumOutputPercent, slope, meanPricePerKwh,
				sdPricePerKwh);
	}
	
	@Override
	protected double getCostsPerKWH(Interval<Double> powerBounds) {
		double pricePerKwh;
		if (powerBounds.max > 1000) {
			pricePerKwh = RoundingHelper.roundToScale(randomizer.createRandomGaussian(0.065, 0.012), 4);
		} else {
			pricePerKwh = RoundingHelper.roundToScale(randomizer.createRandomGaussian(0.15, 0.017), 4);
		}
		return pricePerKwh;
	}
}

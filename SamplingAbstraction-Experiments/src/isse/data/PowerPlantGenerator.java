package isse.data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import utilities.Randomizer;
import utilities.parameters.SimulationParameters;
import de.uniaugsburg.isse.powerplants.PowerPlantData;

/**
 * This class is intended to provide a pool of power plants that are based on a given distribution
 * 
 * @author Alexander Schiendorfer
 *
 */
public class PowerPlantGenerator {

	protected Map<PowerPlantType, SyntheticPowerPlantFactory> factories;
	protected Map<PowerPlantType, Integer> frequency; // how often does a plant of type t occur
	protected int totalPlants; // how many plants do we have
	protected boolean disconnectable; // specify if power plants may be switched off -- affects feasible intervals as
										// well as forceOn-constraint
	protected int minutesPerTimestep = 15;
	protected boolean detailedInertia;

	public PowerPlantGenerator(File biofuelSource, File hydroSource, File gasSource) {
		// initialize factories with appropriate parametrization
		factories = new HashMap<PowerPlantType, SyntheticPowerPlantFactory>();
		frequency = new HashMap<PowerPlantType, Integer>();

		double minimumOutputPercent = 0.35;
		double slope = 0.06;
		double meanPricePerKwh = 0.175;
		double stdDevPricePerKwH = 0.014;

		// TODO evaluate just for more drastic effects
		meanPricePerKwh = 2.3;
		stdDevPricePerKwH = 0.70;

		SyntheticPowerPlantFactory biofuelFactory = new DefaultPowerPlantFactory(PowerPlantType.BIOFUEL, biofuelSource, minimumOutputPercent, slope,
				meanPricePerKwh, stdDevPricePerKwH);
		biofuelFactory.setDisconnectable(disconnectable);
		factories.put(PowerPlantType.BIOFUEL, biofuelFactory);
		totalPlants += biofuelFactory.getNumber();
		frequency.put(PowerPlantType.BIOFUEL, biofuelFactory.getNumber());
		// ---------------------------------------------------------------------

		minimumOutputPercent = 0.00;
		slope = 0.5;
		meanPricePerKwh = 0.15;
		stdDevPricePerKwH = 0.017;

		// TODO dramatization
		meanPricePerKwh = 0.9;
		stdDevPricePerKwH = 0.2;
		
		// HydroPlants have different prices w.r.t. to their capacity, thus customized cost generation function
		SyntheticPowerPlantFactory hydroFactory = new HydroPowerPlantFactory(hydroSource, minimumOutputPercent, slope, meanPricePerKwh, stdDevPricePerKwH);
		hydroFactory.setDisconnectable(disconnectable);
		factories.put(PowerPlantType.HYDRO, hydroFactory);
		totalPlants += hydroFactory.getNumber();
		frequency.put(PowerPlantType.HYDRO, hydroFactory.getNumber());
		// ---------------------------------------------------------------------

		minimumOutputPercent = 0.20;
		slope = 0.2;
		meanPricePerKwh = 0.0865;
		stdDevPricePerKwH = 0.004;

		// TODO dramatization
		meanPricePerKwh = 2.4;
		stdDevPricePerKwH = 1.0;
		// HydroPlants have different prices w.r.t. to their capacity, thus customized cost generation function
		SyntheticPowerPlantFactory gasFactory = new DefaultPowerPlantFactory(PowerPlantType.GAS, gasSource, minimumOutputPercent, slope, meanPricePerKwh,
				stdDevPricePerKwH);
		gasFactory.setDisconnectable(disconnectable);
		factories.put(PowerPlantType.GAS, gasFactory);
		totalPlants += gasFactory.getNumber();
		frequency.put(PowerPlantType.GAS, gasFactory.getNumber());

		System.out.println("+---------------------------------+");
		System.out.println("+    Initialization complete      +");
		for (PowerPlantType type : PowerPlantType.values()) {
			System.out.println("+  - " + type + ": " + frequency.get(type) + " / (" + frequency.get(type) / ((double) totalPlants) + ")");
		}
		System.out.println("+---------------------------------+");

	}

	public static void main(String[] args) {
		SimulationParameters.init();
		Randomizer.getInstance().useDefaultSeed();
		PowerPlantGenerator generator = new PowerPlantGenerator(new File("data/schwaben2012-05-4000kw-biofuel.properties"), new File(
				"data/schwaben2012-05-4000kw-hydro.properties"), new File("data/schwaben2012-05-4000kw-gas.properties"));
	}

	public double getProbability(PowerPlantType type) {
		return ((double) frequency.get(type)) / totalPlants;
	}

	public PowerPlantData createPowerPlant(PowerPlantType nextType) {
		return factories.get(nextType).createPowerPlant();
	}

	public boolean isDisconnectable() {
		return disconnectable;
	}

	public void setDisconnectable(boolean disconnectable) {
		this.disconnectable = disconnectable;
		for (SyntheticPowerPlantFactory factory : factories.values()) {
			factory.setDisconnectable(disconnectable);
		}
	}

	public int getMinutesPerTimestep() {
		return minutesPerTimestep;
	}

	public void setMinutesPerTimestep(int minutesPerTimestep) {
		this.minutesPerTimestep = minutesPerTimestep;
		for (SyntheticPowerPlantFactory factory : factories.values()) {
			factory.setMinutesPerTimestep(minutesPerTimestep);
		}
	}

	public void setDetailedInertia(boolean detailedInertia) {
		this.detailedInertia = detailedInertia;
		for (SyntheticPowerPlantFactory factory : factories.values()) {
			factory.setDetailedInertia(detailedInertia);
		}
	}

	public boolean isDetailedInertia() {
		return detailedInertia;
	}
}

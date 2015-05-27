package isse.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import utilities.Randomizer;
import utilities.RoundingHelper;
import de.uniaugsburg.isse.abstraction.types.Interval;
import de.uniaugsburg.isse.abstraction.types.PiecewiseLinearFunction;
import de.uniaugsburg.isse.constraints.BoundsConstraint;
import de.uniaugsburg.isse.constraints.FixedChangeConstraint;
import de.uniaugsburg.isse.constraints.ForceOnConstraint;
import de.uniaugsburg.isse.constraints.GraduallyOffConstraint;
import de.uniaugsburg.isse.powerplants.PowerPlantData;
import de.uniaugsburg.isse.util.AbstractionParameterLiterals;

/**
 * Expects a set of bounds for nameplate capacity (P_max) to sample P_max and determine all other considered parameters
 * based on that:
 * 
 * P_min, P_delta, costs -------------------------------------------
 * 
 * @author alexander
 *
 */
public class DefaultPowerPlantFactory implements SyntheticPowerPlantFactory {
	/**
	 * Boundaries for the generation of suitable parameters -------------------------------------------
	 */
	protected Interval<Double> nameplateCapacityBounds;
	protected Interval<Double> productionChangeBounds;
	protected Randomizer randomizer;
	protected int counter; // used for ids

	/**
	 * Type-specific parameters -------------------------------------------
	 */
	/**
	 * The power plant's minimal output in % of the maximum output (0 <= minimumOutputPercent <= 1).
	 */
	protected double minimumOutputPercent;

	/**
	 * The rate with which the power plant can change its output (valid values: [0;1])
	 */
	protected double slope;

	/**
	 * The power plants mean price per kw/h.
	 */
	protected double meanPricePerKwh;

	/**
	 * The power plants stddev price per kw/h.
	 */
	protected double sdPricePerKwh;

	/**
	 * Denotes whether the plant may be switched off
	 */
	protected boolean disconnectable;

	/**
	 * Contains information about the distribution over nameplate capacities in this type
	 */
	private Properties distribution;

	/**
	 * Returns how many plants of its type were in the base set
	 */
	protected int number;

	/**
	 * Representing which power plant type
	 */
	protected PowerPlantType type;

	/**
	 * Represents the time difference between two time steps
	 */
	protected int minutesPerTimestep;

	/**
	 * Consider minimal running / stopping times
	 */
	protected boolean detailedInertia = false;

	public DefaultPowerPlantFactory(PowerPlantType type, File distributionFile, double minimumOutputPercent, double slope, double meanPricePerKwh,
			double sdPricePerKwh) {
		super();
		randomizer = Randomizer.getInstance();
		this.minimumOutputPercent = minimumOutputPercent;
		this.slope = slope;
		this.meanPricePerKwh = meanPricePerKwh;
		this.sdPricePerKwh = sdPricePerKwh;
		this.type = type;
		distribution = new Properties();
		FileInputStream input = null;
		number = 0;
		try {
			input = new FileInputStream(distributionFile);
			distribution.load(input);
			number = Integer.parseInt(distribution.getProperty(ParameterLiterals.number));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public PowerPlantData createPowerPlant() {
		PowerPlantData pdata = new PowerPlantData(type.toString() + "_" + (++counter));
		double minMaxOutput = Double.parseDouble(distribution.getProperty(ParameterLiterals.minMaxOutput));
		double maxMaxOutput = Double.parseDouble(distribution.getProperty(ParameterLiterals.maxMaxOutput));
		double meanMaxOutput = Double.parseDouble(distribution.getProperty(ParameterLiterals.meanMaxOutput));
		double stdMaxOutput = Double.parseDouble(distribution.getProperty(ParameterLiterals.sdMaxOutput));

		double nameplateCapacity = Math.min(maxMaxOutput, Math.max(minMaxOutput, randomizer.createRandomGaussian(meanMaxOutput, stdMaxOutput)));
		Interval<Double> powerBoundaries = getPowerBoundaries(nameplateCapacity);
		pdata.setPowerBoundaries(powerBoundaries);

		pdata.put(AbstractionParameterLiterals.CONSRUNNING_INIT, "1"); // always running
		pdata.put(AbstractionParameterLiterals.CONSSTOPPING_INIT, "0");

		// MAX_PROD_CHANGE should give the amount a plant may change from one time step to the next (15 mins)
		pdata.put(AbstractionParameterLiterals.MAX_PROD_CHANGE, Double.toString(getAdjustableOutputPerTimeStep(powerBoundaries)));

		pdata.setFeasibleRegions(getFeasibleRegions(powerBoundaries));

		double costsPerKwH = getCostsPerKWH(powerBoundaries);
		pdata.put(AbstractionParameterLiterals.COSTS_PER_KWH, Double.toString(costsPerKwH));

		PiecewiseLinearFunction costFunction = new PiecewiseLinearFunction(powerBoundaries.min, powerBoundaries.max, costsPerKwH);
		costFunction.prolongAdInfinitum();
		pdata.setCostFunction(costFunction);

		if (!disconnectable) {
			pdata.addConstraint(new ForceOnConstraint());
		}
		pdata.addConstraint(new FixedChangeConstraint(pdata));
		pdata.addConstraint(new BoundsConstraint(pdata));
		pdata.addConstraint(new GraduallyOffConstraint(pdata));
		return pdata;
	}

	@Override
	public boolean isDisconnectable() {
		return disconnectable;
	}

	@Override
	public void setDisconnectable(boolean disconnectable) {
		this.disconnectable = disconnectable;
	}

	protected double getCostsPerKWH(Interval<Double> powerBoundaries) {
		return RoundingHelper.roundToScale(Randomizer.getInstance().createRandomGaussian(meanPricePerKwh, sdPricePerKwh), 4);
	}

	/**
	 * For now just assume the set {[P_min, P_max]} - this could change to include [0,0]
	 * 
	 * @param powerBoundaries
	 * @return
	 */
	private SortedSet<Interval<Double>> getFeasibleRegions(Interval<Double> powerBoundaries) {
		SortedSet<Interval<Double>> returnSet = new TreeSet<Interval<Double>>();
		if (disconnectable && powerBoundaries.min > 0.0) {
			returnSet.add(new Interval<Double>(0.0));
		}
		returnSet.add(powerBoundaries);
		return returnSet;
	}

	protected double getAdjustableOutputPerTimeStep(Interval<Double> powerBoundaries) {
		return minutesPerTimestep * (slope * powerBoundaries.max);
	}

	protected Interval<Double> getPowerBoundaries(double nameplateCapacity) {
		double pMin = minimumOutputPercent * nameplateCapacity;
		return new Interval<Double>(pMin, nameplateCapacity);
	}

	@Override
	public int getNumber() {
		return number;
	}

	@Override
	public int getMinutesPerTimestep() {
		return minutesPerTimestep;
	}

	@Override
	public void setMinutesPerTimestep(int minutesPerTimestep) {
		this.minutesPerTimestep = minutesPerTimestep;
	}

	@Override
	public boolean isDetailedInertia() {
		return detailedInertia;
	}

	@Override
	public void setDetailedInertia(boolean detailedInertia) {
		this.detailedInertia = detailedInertia;
	}

}

package isse.experiments;

import isse.data.PowerPlantGenerator;
import isse.data.PowerPlantType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import utilities.Randomizer;
import utilities.parameters.SimulationParameters;
import de.uniaugsburg.isse.abstraction.GeneralAbstraction;
import de.uniaugsburg.isse.abstraction.SamplingAbstraction;
import de.uniaugsburg.isse.abstraction.types.PiecewiseLinearFunction;
import de.uniaugsburg.isse.powerplants.PowerPlantData;
import de.uniaugsburg.isse.powerplants.PowerPlantSource;
import de.uniaugsburg.isse.solver.AbstractModel;
import de.uniaugsburg.isse.solver.CplexModel;
import de.uniaugsburg.isse.solver.CplexSolver;
import de.uniaugsburg.isse.util.AbstractionParameterLiterals;
import de.uniaugsburg.isse.util.PowerPlantUtil;

/**
 * This class serves to create an AVPP purely consisting of physical power
 * plants and perform sampling to obtain data of functional dependencies (rate
 * of change and costs)
 * 
 * @author alexander
 *
 */
public class SamplingDataGeneration implements PowerPlantSource {

	private PowerPlantGenerator generator;
	private String jobName; // for file reference

	// ------------------------------
	// Parameters for the sampling data generation
	protected int numberOfPlants = 3;

	// should power plants be drawn to the available distribution of types or
	// uniformly
	protected Distribution distribution = Distribution.DATA;

	// how many sampling points should be calculated
	protected int samplingPoints = 5;

	// this can be minimize costs; minimize P[1]
	protected final Collection<String> minimizationObjectives;

	// this can only be maximize P[1]
	protected final Collection<String> maximizationObjectives;

	// adjustable output is steered by minutes between time steps
	protected int minutesPerTimestep = 15;

	/**
	 * flags to easily turn on/off sampling objectives
	 */
	protected boolean sampleCosts = true;
	protected boolean samplePdeltaPos = false;
	protected boolean samplePdeltaNeg = false;

	public Collection<String> getMinimizationObjectives() {
		return minimizationObjectives;
	}

	public Collection<String> getMaximizationObjectives() {
		return maximizationObjectives;
	}

	public SamplingDataGeneration(PowerPlantGenerator generator, String jobName) {
		this.generator = generator;
		this.jobName = jobName;
		minimizationObjectives = new ArrayList<String>();
		maximizationObjectives = new ArrayList<String>();
	}

	public void run() {
		// add some objectives -
		String prodSucc = AbstractionParameterLiterals.DEXP_POWER + "Succ";
		if (samplePdeltaPos)
			maximizationObjectives.add(prodSucc);
		if (samplePdeltaNeg)
			minimizationObjectives.add(prodSucc);
		if (sampleCosts)
			minimizationObjectives.add(AbstractionParameterLiterals.DEXP_COSTS
					+ "Init");

		// first get the AVPP
		Collection<PowerPlantData> powerPlants = drawPowerPlants();

		// make a single root AVPP
		PowerPlantData avpp = new PowerPlantData("rootAVPP");
		avpp.setAVPP(true);

		// general abstraction to obtain bounds and wholes
		GeneralAbstraction ga = new GeneralAbstraction();
		ga.setPowerPlants(powerPlants);
		ga.perform();
		ga.print();

		CplexSolver solver = new CplexSolver();
		AbstractModel model = new CplexModel();
		model.setUseSoftConstraints(false);

		SamplingAbstraction sa = new SamplingAbstraction(
				ga.getFeasibleRegions(), ga.getHoles());
		PowerPlantUtil.populateDefaultSamplingModel(solver, model, avpp,
				powerPlants);
		sa.setSolver(solver);
		sa.setMaximizationDecisionExpressions(maximizationObjectives);
		sa.setMinimizationDecisionExpressions(minimizationObjectives);

		sa.perform(samplingPoints);

		// print piecewise linear functions
		if (sampleCosts) {
			PiecewiseLinearFunction costFunction = sa
					.getPiecewiseLinearFunction(
							AbstractionParameterLiterals.DEXP_COSTS + "Init",
							true);
			PowerPlantUtil.writeData("results-sampling/" + jobName
					+ "_costs.csv", costFunction);
		}

		if (samplePdeltaPos) {
			PiecewiseLinearFunction pdeltaPos = sa.getPiecewiseLinearFunction(
					AbstractionParameterLiterals.DEXP_POWER + "Succ", false);
			PowerPlantUtil.writeData("results-sampling/" + jobName
					+ "_pdelta-pos.csv", pdeltaPos);
		}

		if (samplePdeltaNeg) {
			PiecewiseLinearFunction pdeltaNeg = sa.getPiecewiseLinearFunction(
					AbstractionParameterLiterals.DEXP_POWER + "Succ", true);
			PowerPlantUtil.writeData("results-sampling/" + jobName
					+ "_pdelta-neg.csv", pdeltaNeg);
		}

	}

	public List<PowerPlantData> drawPowerPlants() {
		List<PowerPlantData> powerPlants = new ArrayList<PowerPlantData>(
				numberOfPlants);
		double[] typeProbabilities = new double[PowerPlantType.values().length];
		for (int i = 0; i < PowerPlantType.values().length; ++i) {
			PowerPlantType type = PowerPlantType.getType(i);
			if (distribution == Distribution.DATA) {
				typeProbabilities[i] = generator.getProbability(type);
			} else { // use uniform distribution
				typeProbabilities[i] = 1.0 / typeProbabilities.length;
			}
		}

		Randomizer randomizer = Randomizer.getInstance();
		for (int i = 0; i < numberOfPlants; ++i) {
			int nextTypeIndex = randomizer.rouletteWheel(typeProbabilities);
			PowerPlantType nextType = PowerPlantType.getType(nextTypeIndex);
			PowerPlantData nextPlant = generator.createPowerPlant(nextType);

			powerPlants.add(nextPlant);
		}
		return powerPlants;
	}

	public static void main(String[] args) {
		SimulationParameters.init();
		Randomizer.getInstance().useDefaultSeed();

		PowerPlantGenerator generator = new PowerPlantGenerator(new File(
				"data/schwaben2012-05-4000kw-biofuel.properties"), new File(
				"data/schwaben2012-05-4000kw-hydro.properties"), new File(
				"data/schwaben2012-05-4000kw-gas.properties"));
		generator.setDisconnectable(false);

		SamplingDataGeneration generation = new SamplingDataGeneration(
				generator, "firstTrial");

		generation.run();
	}

	public int getNumberOfPlants() {
		return numberOfPlants;
	}

	public void setNumberOfPlants(int numberOfPlants) {
		this.numberOfPlants = numberOfPlants;
	}

	public Distribution getDistribution() {
		return distribution;
	}

	public void setDistribution(Distribution distribution) {
		this.distribution = distribution;
	}

	public int getSamplingPoints() {
		return samplingPoints;
	}

	public void setSamplingPoints(int samplingPoints) {
		this.samplingPoints = samplingPoints;
	}

	public boolean isSampleCosts() {
		return sampleCosts;
	}

	public boolean isSamplePdeltaPos() {
		return samplePdeltaPos;
	}

	public boolean isSamplePdeltaNeg() {
		return samplePdeltaNeg;
	}

	public int getMinutesPerTimestep() {
		return minutesPerTimestep;
	}

	public void setMinutesPerTimestep(int minutesPerTimestep) {
		this.minutesPerTimestep = minutesPerTimestep;
	}

	public void setSampleCosts(boolean sampleCosts) {
		this.sampleCosts = sampleCosts;
	}

	public void setSamplePdeltaPos(boolean samplePdeltaPos) {
		this.samplePdeltaPos = samplePdeltaPos;
	}

	public void setSamplePdeltaNeg(boolean samplePdeltaNeg) {
		this.samplePdeltaNeg = samplePdeltaNeg;
	}
}

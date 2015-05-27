package de.uniaugsburg.isse;

import java.io.FileInputStream;
import java.util.Properties;

import de.uniaugsburg.isse.cplex.CPLEXSolverFacade;
import de.uniaugsburg.isse.experiments.Experiment;
import de.uniaugsburg.isse.experiments.ExperimentParameterLiterals;
import de.uniaugsburg.isse.experiments.ExperimentSeries;
import de.uniaugsburg.isse.experiments.HierarchyType;
import de.uniaugsburg.isse.solver.CplexSolverFactory;

/**
 * This class runs experiments using the csp model abstraction experiment suite and offers a CPLEX solver - this is done
 * to avoid having the experiment suite depend on CPLEX binaries
 * 
 * @author alexander
 * 
 */
public class ExperimentRunner {

	public Experiment getExperiment(Properties prop) {
		Experiment exp = new Experiment();
		exp.setTimeHorizon(readProperty(prop, "timeHorizon"));
		exp.setExperimentHorizon(readProperty(prop, "experimentHorizon"));
		exp.setNumberOfPlants(ExperimentParameterLiterals.NumberPlants.lookup(readProperty(prop, "numberOfPlants")));
		exp.setCountPlants(readProperty(prop, "countPlants"));
		exp.setPlantsPerAvpp(readProperty(prop, "plantsPerAvpp"));
		exp.setSamplingPoints(readProperty(prop, "samplingPoints"));
		exp.setHierarchyType(HierarchyType.valueOf(prop.getProperty("hierarchyType")));
		exp.setDisconnectable(readProperty(prop, "disconnectable"));
		exp.setAvppsRandomSeed(readProperty(prop, "avppsRandomSeed"));
		exp.setHierarchyRandomSeed(readProperty(prop, "hierarchyRandomSeed"));
		exp.setInitialStatesSeed(readProperty(prop, "initialStatesSeed"));
		exp.setAvppsPerAvpp(readProperty(prop, "avppsPerAvpp"));
		exp.setOriginatingProperties(prop);
		exp.setSolveCentrally(readProperty(prop, "solveCentrally", 1) == 1);
		exp.setSolveHierarchically(readProperty(prop, "solveHierarchically", 1) == 1);
		exp.setUseStaticSampling(readProperty(prop, "useStaticSampling", 1) == 1);
		exp.setUseTemporalAbstraction(readProperty(prop, "useTemporalAbstraction", 1) == 1);
		exp.setMinutesPerTimestep(readProperty(prop, "minutesPerTimestep", 15));
		exp.setDetailedInertia(readProperty(prop, "detailedInertia", 0) == 1);
		exp.setUseCostsInCents(readProperty(prop, "useCostsInCents", 0) == 1);
		exp.setFeaturesAlgorithm(readProperty(prop, "featuresAlgorithm", 0) == 1);
		exp.setInitialSamplingPoints(readProperty(prop, "initialSamplingPoints", 15));
		return exp;
	}

	private int readProperty(Properties prop, String key, int defaultVal) {
		int res = defaultVal;
		try {
			res = Integer.parseInt(prop.getProperty(key));
		} catch (Exception e) {
			res = defaultVal;
		}

		return res;
	}

	public Experiment getExperiment(String fileName) {

		Properties prop = new Properties();

		try {
			prop.load(new FileInputStream(fileName));
			return getExperiment(prop);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private int readProperty(Properties prop, String key) {
		return Integer.parseInt(prop.getProperty(key));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int nRuns = 5;
		String propertiesFile = "experiments/experiment1.properties";
		ExperimentRunner runner = new ExperimentRunner();
		Experiment exp = runner.getExperiment(propertiesFile);
		exp.setSolverFacade(new CPLEXSolverFacade());
		exp.setSolverFactory(new CplexSolverFactory());
		exp.setUseSamplingAbstraction(true);

		// make a series of experiments out of it
		ExperimentSeries series = new ExperimentSeries();

		// convert seeds to an array
		long[] avppsRandomSeeds = new long[nRuns];
		avppsRandomSeeds[0] = exp.getAvppsRandomSeed();

		long[] hierarchyRandomSeeds = new long[nRuns];
		hierarchyRandomSeeds[0] = exp.getHierarchyRandomSeed();

		long[] initialStatesSeeds = new long[nRuns];
		initialStatesSeeds[0] = exp.getInitialStatesSeed();

		// quite some amateurish work to get new random seeds but it ought to do the job
		for (int i = 1; i < nRuns; ++i) {
			avppsRandomSeeds[i] = (avppsRandomSeeds[i - 1] * 5673) % 6553;
			hierarchyRandomSeeds[i] = (hierarchyRandomSeeds[i - 1] * 5673) % 6553;
			initialStatesSeeds[i] = (initialStatesSeeds[i - 1] * 5673) % 6553;
		}
		series.setAvppsRandomSeeds(avppsRandomSeeds);
		series.setExperiment(exp);
		series.setHierarchyRandomSeed(hierarchyRandomSeeds);
		series.setInitialStatesSeeds(initialStatesSeeds);
		series.run();
	}

}

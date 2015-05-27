package isse.experiments;

import isse.data.PowerPlantGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import utilities.Randomizer;
import utilities.parameters.SimulationParameters;
import de.uniaugsburg.isse.ExperimentRunner;
import de.uniaugsburg.isse.cplex.CPLEXSolverFacade;
import de.uniaugsburg.isse.experiments.Experiment;
import de.uniaugsburg.isse.experiments.ExperimentSeries;
import de.uniaugsburg.isse.solver.CplexSolverFactory;

/**
 * Designed to create a series of experiments with different AVPPs based on the "generative model" and different
 * sampling point selection strategies
 * 
 * @author alexander
 *
 */
public class SamplingExperimentSeries {

	public static void runFromProperties(Properties prop, int nRuns, String symbolicName) {
		ExperimentRunner runner = new ExperimentRunner();
		Experiment exp = runner.getExperiment(prop);

		Randomizer.getInstance().useFixedSeed(exp.getAvppsRandomSeed());

		PowerPlantGenerator generator = new PowerPlantGenerator(new File("data/schwaben2012-05-4000kw-biofuel.properties"), new File(
				"data/schwaben2012-05-4000kw-hydro.properties"), new File("data/schwaben2012-05-4000kw-gas.properties"));
		generator.setDisconnectable(exp.isDisconnectable());
		generator.setMinutesPerTimestep(exp.getMinutesPerTimestep());
		generator.setDetailedInertia(exp.isDetailedInertia());

		SamplingDataGeneration generation = new SamplingDataGeneration(generator, "AVPP-Generative Model");
		generation.setNumberOfPlants(exp.getCountPlants());

		exp.setSource(generation);
		exp.setSolverFacade(new CPLEXSolverFacade());
		exp.setSolverFactory(new CplexSolverFactory());
		exp.setUseSamplingAbstraction(true);

		// make a series of experiments out of it
		ExperimentSeries series = new ExperimentSeries();
		series.setSymbolicName(symbolicName);

		// convert seeds to an array
		long[] avppsRandomSeeds = new long[nRuns];
		avppsRandomSeeds[0] = exp.getAvppsRandomSeed();

		long[] hierarchyRandomSeeds = new long[nRuns];
		hierarchyRandomSeeds[0] = exp.getHierarchyRandomSeed();

		long[] initialStatesSeeds = new long[nRuns];
		initialStatesSeeds[0] = exp.getInitialStatesSeed();

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

	public static void main(String[] args) throws FileNotFoundException, IOException {
		SimulationParameters.init();
		Randomizer.getInstance().useDefaultSeed();

		String propertiesFile = "experiments/sampling.properties";
		int nRuns = 1;
		String symbolicName = "";

		if (args.length >= 3) {
			// first argument needs to specify the properties file
			propertiesFile = args[0];

			// second the number of runs for one properties file config
			nRuns = Integer.parseInt(args[1]);

			// third symbolic name for experiment data
			symbolicName = args[2];

			System.out.println("p " + propertiesFile + " n " + nRuns + " sn " + symbolicName);
		}

		Properties prop = new Properties();
		prop.load(new FileInputStream(propertiesFile));
		runFromProperties(prop, nRuns, symbolicName);
	}
}

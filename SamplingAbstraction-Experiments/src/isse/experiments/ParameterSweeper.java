package isse.experiments;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Properties;

import utilities.Randomizer;
import utilities.parameters.SimulationParameters;

/**
 * This class is intended to enable fast generation of cubes of experiments based on collections of values that ought to
 * be tried based on a predefined properties file
 * 
 * @author alexander
 *
 */
public class ParameterSweeper {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		SimulationParameters.init();
		Randomizer.getInstance().useDefaultSeed();

		String propertiesFile = "experiments/sampling.properties";
		int nRuns = 1;
		String sweepName = "";

		if (args.length >= 3) {
			// first argument needs to specify the properties file
			propertiesFile = args[0];

			// second the number of runs for one properties file config
			nRuns = Integer.parseInt(args[1]);

			// third sweep input file
			sweepName = args[2];
		}

		SweepReader reader = new SweepReader();
		Collection<SweepConfig> propertyDeltas = reader.read(sweepName);

		Properties prop = new Properties();
		prop.load(new FileInputStream(propertiesFile));

		for (SweepConfig sweepConfig : propertyDeltas) {
			System.out.println(sweepConfig.getSymbolicName());
			for (Entry<String, String> propertyChange : sweepConfig.getPropertyDelta().entrySet()) {
				prop.setProperty(propertyChange.getKey(), propertyChange.getValue());
			}
			SamplingExperimentSeries.runFromProperties(prop, nRuns, sweepConfig.getSymbolicName());
		}

	}

}

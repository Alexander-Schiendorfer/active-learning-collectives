package isse.experiments;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import utilities.Randomizer;
import utilities.parameters.SimulationParameters;

/**
 * Measures scheduling running times for varying sizes of AVPPs starting from 5 going up to 100
 * 
 * @author alexander
 *
 */
public class ScalabilitySweep {

	private static int maxPlants = 7;
	private static int minPlants = 5;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		SimulationParameters.init();
		Randomizer.getInstance().useDefaultSeed();

		String propertiesFile = "experiments/scalability.properties";
		int nRuns = 15;
		String symbolicName = "";

		if (args.length >= 2) {
			// first argument needs to specify the properties file
			minPlants = Integer.parseInt(args[0]);
			maxPlants = Integer.parseInt(args[1]);
		}

		for (int n = minPlants; n <= maxPlants; ++n) {
			Properties prop = new Properties();
			prop.load(new FileInputStream(propertiesFile));
			prop.setProperty("countPlants", Integer.toString(n));
			SamplingExperimentSeries.runFromProperties(prop, nRuns, "scalability_" + n);
		}

	}

}

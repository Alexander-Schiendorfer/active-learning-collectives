package isse.experiments;

import isse.data.PowerPlantGenerator;
import isse.data.SamplingDataCreationJob;

import java.io.File;

import utilities.Randomizer;
import utilities.parameters.SimulationParameters;

/**
 * Provides the same functionality as {@link SamplingDataCreationJob} but offers to explicitly state the involved number
 * of plants etc
 * 
 * @author alexander
 *
 */
public class SamplingDataManually {

	protected int[] powerPlants = { 10 };
	protected int[] samplingPoints = { 5, 10, 25 };
	protected int drawAvpps = 1;
	private PowerPlantGenerator generator;

	public SamplingDataManually(PowerPlantGenerator generator) {
		this.generator = generator;
	}

	public void run() {
		for (int plants : powerPlants) {
			for (int sp : samplingPoints) {
				for (int avppInd = 0; avppInd < drawAvpps; ++avppInd) {
					String ident = "plants_" + plants + "_avpp_" + avppInd + "_sps_" + sp;
					SamplingDataGeneration generation = new SamplingDataGeneration(generator, ident);
					generation.setMinutesPerTimestep(1);
					generator.setMinutesPerTimestep(1);
					generator.setDisconnectable(true);
					generation.setNumberOfPlants(plants);
					generation.setSamplingPoints(sp);
					generation.setSamplePdeltaNeg(false);
					generation.setSamplePdeltaPos(false);

					System.out.println(ident);
					generation.run();
					Randomizer.getInstance().useFixedSeed(1337);
					generator = new PowerPlantGenerator(new File("data/schwaben2012-05-4000kw-biofuel.properties"), new File(
							"data/schwaben2012-05-4000kw-hydro.properties"), new File("data/schwaben2012-05-4000kw-gas.properties"));

				}
			}
		}
	}

	public static void main(String[] args) {
		SimulationParameters.init();
		Randomizer.getInstance().useFixedSeed(1337);

		PowerPlantGenerator generator = new PowerPlantGenerator(new File("data/schwaben2012-05-4000kw-biofuel.properties"), new File(
				"data/schwaben2012-05-4000kw-hydro.properties"), new File("data/schwaben2012-05-4000kw-gas.properties"));
		generator.setDisconnectable(true);
		generator.setMinutesPerTimestep(1);

		SamplingDataManually job = new SamplingDataManually(generator);
		job.run();
	}
}

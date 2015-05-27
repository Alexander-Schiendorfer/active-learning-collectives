package isse.data;

import java.io.File;

import utilities.Randomizer;
import utilities.parameters.SimulationParameters;
import isse.experiments.SamplingDataGeneration;

/**
 * This class collects everything needed
 * to perform a reasonable data collection run
 * @author alexander
 *
 */
public class SamplingDataCreationJob {

	protected int minPowerPlants = 50;
	protected int maxPowerPlants = 50;
	protected int powerPlantIncrement = 5;
	protected int drawAvpps = 1;
	protected int samplingPoints = 10;
	private PowerPlantGenerator generator;
	
	public SamplingDataCreationJob(PowerPlantGenerator generator) {
		this.generator = generator;
	}

	public void run() {
		for(int plants = minPowerPlants; plants <= maxPowerPlants; plants += 5) {
			for(int avppInd = 0; avppInd < drawAvpps; ++avppInd) {
				String ident = "plants_"+plants+"_avpp_"+avppInd +"_sps_"+samplingPoints;
				SamplingDataGeneration generation = new SamplingDataGeneration(generator, ident);
				generation.setNumberOfPlants(plants);
				generation.setSamplingPoints(samplingPoints);
				System.out.println(ident);
				generation.run();
			}
		}
	}
	
	public static void main(String[] args) {
		SimulationParameters.init();
		Randomizer.getInstance().useDefaultSeed();
		
		PowerPlantGenerator generator = new PowerPlantGenerator(new File("data/schwaben2012-05-4000kw-biofuel.properties"), new File("data/schwaben2012-05-4000kw-hydro.properties"), new File("data/schwaben2012-05-4000kw-gas.properties"));
		generator.setDisconnectable(true);
		generator.setMinutesPerTimestep(15);
	
		SamplingDataCreationJob job = new SamplingDataCreationJob(generator);
		job.run();
	}
}

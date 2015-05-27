package de.uniaugsburg.isse.powerplants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.uniaugsburg.isse.RandomManager;
import de.uniaugsburg.isse.data.PowerplantReader;

public class FilePowerPlantSource implements PowerPlantSource {

	private String fileNameBio;
	private String fileNameGas;

	public FilePowerPlantSource(String fileNameBio, String fileNameGas) {
		this.fileNameBio = fileNameBio;
		this.fileNameGas = fileNameGas;
	}
	
	@Override
	public List<PowerPlantData> drawPowerPlants() {

		PowerplantReader reader = new PowerplantReader();
		Collection<PowerPlantData> bioPlants = reader.readPlants(fileNameBio);
		Collection<PowerPlantData> gasPlants = reader.readPlants(fileNameGas);

		// 2. equip power plants with random constraints/ change rate has to be
		// set
		RandomConstraintBuilder.addChangeConstraints(bioPlants);
		RandomConstraintBuilder.addChangeConstraints(gasPlants);

		// add stop time constraints to bio plants
		RandomConstraintBuilder.addStopTimeConstraints(bioPlants);

		ArrayList<PowerPlantData> allPlants = new ArrayList<PowerPlantData>(bioPlants.size() + gasPlants.size());
		allPlants.addAll(bioPlants);
		allPlants.addAll(gasPlants);
		// shuffle plants
		Collections.shuffle(allPlants, RandomManager.getRandom());
		return allPlants;
	}

}

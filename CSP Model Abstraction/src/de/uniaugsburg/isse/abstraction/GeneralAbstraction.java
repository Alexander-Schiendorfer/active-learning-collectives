package de.uniaugsburg.isse.abstraction;

import java.util.Collection;
import java.util.SortedSet;

import de.uniaugsburg.isse.abstraction.merging.HoleDetection;
import de.uniaugsburg.isse.abstraction.types.Interval;
import de.uniaugsburg.isse.powerplants.PowerPlantData;
import de.uniaugsburg.isse.util.PowerPlantUtil;

/**
 * Performs the general boundary detection for a set of power plants - detects holes in power supply
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class GeneralAbstraction {
	protected Collection<PowerPlantData> powerPlants;
	protected SortedSet<Interval<Double>> generalFeasibleRegions;
	protected Collection<Interval<Double>> generalHoles;

	public final void perform() {
		performNew();
	}

	public final void performNew() {
		HoleDetection hd = new HoleDetection();
		Collection<Collection<Interval<Double>>> plantsFeasibleRegions = PowerPlantUtil.extractFromPlants(powerPlants);

		// generalHoles = hd.detectSupplyHoles(plantsFeasibleRegions);
		generalHoles = hd.detectSupplyHolesNew(plantsFeasibleRegions);
		generalFeasibleRegions = hd.getIntervalList();
	}

	public SortedSet<Interval<Double>> getFeasibleRegions() {
		return generalFeasibleRegions;
	}

	public Collection<Interval<Double>> getHoles() {
		return generalHoles;
	}

	public Collection<PowerPlantData> getPowerPlants() {
		return powerPlants;
	}

	public void setPowerPlants(Collection<PowerPlantData> powerPlants) {
		this.powerPlants = powerPlants;
	}

	public void print() {
		System.out.println("Feasible regions: ");
		for (Interval<Double> fr : getFeasibleRegions()) {
			System.out.println(fr);
		}
		System.out.println("----------- and holes");
		for (Interval<Double> fr : getHoles()) {
			System.out.println(fr);
		}

	}

	public void setGeneralFeasibleRegions(SortedSet<Interval<Double>> generalFeasibleRegions) {
		this.generalFeasibleRegions = generalFeasibleRegions;
	}

	public void setGeneralHoles(Collection<Interval<Double>> generalHoles) {
		this.generalHoles = generalHoles;
	}
}

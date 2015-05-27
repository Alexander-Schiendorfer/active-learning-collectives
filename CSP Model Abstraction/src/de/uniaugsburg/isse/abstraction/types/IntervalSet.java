package de.uniaugsburg.isse.abstraction.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

/**
 * Holder class for feasible regions and supply holes
 * 
 * @author alexander
 * 
 */
public class IntervalSet {
	protected List<SortedSet<Interval<Double>>> allFeasibleRegions;

	public IntervalSet(ArrayList<SortedSet<Interval<Double>>> allFeasibleRegionsTemp, ArrayList<Collection<Interval<Double>>> allHolesTemp) {
		this.allFeasibleRegions = allFeasibleRegionsTemp;
		this.allHoles = allHolesTemp;
	}

	public List<SortedSet<Interval<Double>>> getAllFeasibleRegions() {
		return this.allFeasibleRegions;
	}

	protected List<Collection<Interval<Double>>> allHoles;

	public List<Collection<Interval<Double>>> getAllHoles() {
		return this.allHoles;
	}
}

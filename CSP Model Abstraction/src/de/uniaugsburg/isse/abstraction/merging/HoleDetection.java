package de.uniaugsburg.isse.abstraction.merging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uniaugsburg.isse.abstraction.merging.MergeLists.List;
import de.uniaugsburg.isse.abstraction.types.Interval;
import de.uniaugsburg.isse.util.PowerPlantUtil;

/**
 * Wraps merge list internal details and performs the core of the abstraction algorithm i.e. merge intervals to find
 * feasible regions and supply holes
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class HoleDetection {
	private SortedSet<Interval<Double>> intervalList;
	private Collection<Interval<Double>> holesList;

	public Collection<Interval<Double>> getHolesList() {
		return holesList;
	}

	public SortedSet<Interval<Double>> getIntervalList() {
		return intervalList;
	}

	/**
	 * Returns a Collection of holes that are not feasible in the current setting; Uses a singly linked list
	 * implementation for efficiency
	 * 
	 * @param plantsFeasibleRegions
	 * @return
	 */
	public Collection<Interval<Double>> detectSupplyHolesNew(Collection<Collection<Interval<Double>>> plantsFeasibleRegions) {
		boolean isFirst = true;
		Collection<Interval<Double>> minIntervalSet = null;

		// an empty list should first raise concerns but be treated as [0,0]
		// therefore correct an empty feasible regions collection by a singleton
		// { [0,0] }
		Collection<Collection<Interval<Double>>> plantsFeasibleRegionsCorrected = new ArrayList<Collection<Interval<Double>>>(), plantsActualFeasibleRegions = null;
		for (Collection<Interval<Double>> singlePlantFeasibleRegions : plantsFeasibleRegions) {
			if (singlePlantFeasibleRegions.isEmpty()) {
				Collection<Interval<Double>> zero = new ArrayList<Interval<Double>>(1);
				zero.add(new Interval<Double>(0.0));
				plantsFeasibleRegionsCorrected.add(zero);
			} else {
				plantsFeasibleRegionsCorrected.add(singlePlantFeasibleRegions);
			}
		}

		// do interval merging and combining
		boolean useSafe = true;
		if (useSafe) {
			plantsActualFeasibleRegions = plantsFeasibleRegionsCorrected;
		} else
			plantsActualFeasibleRegions = plantsFeasibleRegions;

		// during bootstrapping this collection could be empty
		if (!plantsActualFeasibleRegions.isEmpty()) {

			isFirst = true;
			List aggregateFeasibleRegionsHead = null;

			for (Collection<Interval<Double>> singlePlantFeasibleRegions : plantsActualFeasibleRegions) {
				// combine all intervals in this plant's feasible regions with
				// each of the already existing intervals
				if (isFirst) {
					aggregateFeasibleRegionsHead = List.fromCollection(singlePlantFeasibleRegions);
					isFirst = false;
				} else {
					// perform a plus on sets with all already feasible regions and the incomings (includes merging)
					aggregateFeasibleRegionsHead = PowerPlantUtil.plusSets(aggregateFeasibleRegionsHead, singlePlantFeasibleRegions);
				}
			}
			intervalList = MergeLists.toJavaSet(aggregateFeasibleRegionsHead);

			// collect holes
			Collection<Interval<Double>> holes = new TreeSet<Interval<Double>>();
			if (aggregateFeasibleRegionsHead.size() > 1) {
				List curr = aggregateFeasibleRegionsHead;
				while (curr.getNext() != null) {
					holes.add(new Interval<Double>(curr.getInterval().max, curr.getNext().getInterval().min));
					curr = curr.getNext();
				}
			}
			holesList = holes;
			return holes;
		} else {
			intervalList = new TreeSet<Interval<Double>>(); // empty set
			// suffices
			holesList = new TreeSet<Interval<Double>>();
			return holesList;//
		}
	}

	@Deprecated
	public Collection<Interval<Double>> detectSupplyHoles(Collection<Collection<Interval<Double>>> plantIntervals) {
		boolean first = true;
		Collection<Interval<Double>> minIntervalSet = null;

		// an empty list should first raise concerns but be treated as [0,0]
		Collection<Collection<Interval<Double>>> plantSafeIntervals = new ArrayList<Collection<Interval<Double>>>(), usedIntervals = null;
		for (Collection<Interval<Double>> plantIntervalSet : plantIntervals) {
			if (plantIntervalSet.isEmpty()) {
				Collection<Interval<Double>> zero = new ArrayList<Interval<Double>>(1);
				zero.add(new Interval<Double>(0.0));
				plantSafeIntervals.add(zero);
			} else {
				plantSafeIntervals.add(plantIntervalSet);
			}
		}

		// do interval merging and combining
		boolean useSafe = true;
		if (useSafe) {
			usedIntervals = plantSafeIntervals;
		} else
			usedIntervals = plantIntervals;

		for (Collection<Interval<Double>> plantIntervalSet : usedIntervals) {
			if (first) {
				minIntervalSet = plantIntervalSet;
				first = false;
			} else {
				minIntervalSet = PowerPlantUtil.plusSets(minIntervalSet, plantIntervalSet);
			}
		}

		first = true;
		List listHead = null;

		// can happen to be empty during bootstrapping
		if (minIntervalSet != null && !minIntervalSet.isEmpty()) {
			for (Interval<Double> resultingInterval : minIntervalSet) {
				// System.out.println("Merging in ... " + resultingIntervals);
				if (first) {
					listHead = new List(resultingInterval, null);
					first = false;
				} else {
					listHead = List.mergeIn(listHead, resultingInterval);
				}
				// MergeLists.printList(listHead);
			}
			intervalList = MergeLists.toJavaSet(listHead);

			// collect holes
			Collection<Interval<Double>> holes = new TreeSet<Interval<Double>>();
			if (listHead.size() > 1) {
				List curr = listHead;
				while (curr.getNext() != null) {
					holes.add(new Interval<Double>(curr.getInterval().max, curr.getNext().getInterval().min));
					curr = curr.getNext();
				}
			}
			holesList = holes;
			return holes;
		} else {
			intervalList = new TreeSet<Interval<Double>>(); // empty set
															// suffices
			holesList = new TreeSet<Interval<Double>>();
			return holesList;//
		}
	}
}

package de.uniaugsburg.isse.abstraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.uniaugsburg.isse.abstraction.types.Interval;
import de.uniaugsburg.isse.powerplants.PowerPlantData;
import de.uniaugsburg.isse.util.PowerPlantUtil;

/**
 * Tries to find out how many power plants can be feasibly used for general abstraction etc
 * 
 * @author guesticac
 *
 */
public class ScalabilityTest {

	private Collection<PowerPlantData> plants;

	private void setup(int size) {
		double pMin = 2.0;
		double pMax = 5.0;

		plants = new ArrayList<PowerPlantData>(size);
		for (int i = 0; i < size; ++i) {
			PowerPlantData plant = PowerPlantUtil.getPowerPlantFixed("AVPP_" + i, pMin, pMax, 0.5);
			plant.setFeasibleRegions(new TreeSet<Interval<Double>>(Arrays.asList(new Interval<Double>(0.0), new Interval<Double>(pMin, pMax))));
			plants.add(plant);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testTwentyPlants() {
		setup(20);
		GeneralAbstraction ga = new GeneralAbstraction();
		ga.setPowerPlants(plants);
		ga.perform();

		SortedSet<Interval<Double>> feasibleRegions = ga.getFeasibleRegions();
		Collection<Interval<Double>> supplyHoles = ga.getHoles();

		List<Interval<Double>> expectedRegions = Arrays.asList(new Interval<Double>(0.0), new Interval<Double>(2.0, 100.0));
		List<Interval<Double>> expectedHoles = Arrays.asList(new Interval<Double>(0.0, 2.0));

		compareCollections(expectedRegions, feasibleRegions);
		compareCollections(expectedHoles, supplyHoles);
		ga.print();
	}

	private void compareCollections(List<Interval<Double>> expecteds, Collection<Interval<Double>> actuals) {
		Assert.assertEquals(expecteds.size(), actuals.size());
		for (Interval<Double> expectedInterval : expecteds) {
			Assert.assertTrue(actuals.contains(expectedInterval));
		}

	}

	@Test
	@Ignore
	public void testFiftyPlants() {
		setup(50);
		GeneralAbstraction ga = new GeneralAbstraction();
		ga.setPowerPlants(plants);
		ga.perform();

		SortedSet<Interval<Double>> feasibleRegions = ga.getFeasibleRegions();
		Collection<Interval<Double>> supplyHoles = ga.getHoles();

		List<Interval<Double>> expectedRegions = Arrays.asList(new Interval<Double>(0.0), new Interval<Double>(2.0, 250.0));
		List<Interval<Double>> expectedHoles = Arrays.asList(new Interval<Double>(0.0, 2.0));

		compareCollections(expectedRegions, feasibleRegions);
		compareCollections(expectedHoles, supplyHoles);
		ga.print();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPlusSets() {
		// ought to test if plus can be lifted correctly to sets
		Collection<Interval<Double>> firstIntervals = Arrays.asList(new Interval<Double>(0.0), new Interval<Double>(2.0, 5.0));
		Collection<Interval<Double>> secondIntervals = Arrays.asList(new Interval<Double>(0.0), new Interval<Double>(4.0, 10.0));

		Collection<Interval<Double>> expecteds = Arrays.asList(new Interval<Double>(0.0), new Interval<Double>(4.0, 10.0), new Interval<Double>(2.0, 5.0),
				new Interval<Double>(6.0, 15.0));
		Collection<Interval<Double>> result = PowerPlantUtil.plusSets(firstIntervals, secondIntervals);

		Assert.assertEquals(expecteds.size(), result.size());
		for (Interval<Double> expectedInterval : expecteds) {
			Assert.assertTrue(result.contains(expectedInterval));
		}
	}
}

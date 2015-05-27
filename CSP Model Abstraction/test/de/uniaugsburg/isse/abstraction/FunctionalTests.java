package de.uniaugsburg.isse.abstraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import de.uniaugsburg.isse.abstraction.merging.HoleDetection;
import de.uniaugsburg.isse.abstraction.types.Interval;
import de.uniaugsburg.isse.powerplants.PowerPlantData;
import de.uniaugsburg.isse.util.PowerPlantUtil;

public class FunctionalTests {

	/**
	 * Two avpps with their respective intervals
	 */
	@Test
	public void testTwoAvpps() {
		List<SortedSet<Interval<Double>>> firstSet = new ArrayList<SortedSet<Interval<Double>>>(2);
		SortedSet<Interval<Double>> firstSetT1 = new TreeSet<Interval<Double>>();
		firstSetT1.add(new Interval<Double>(16.0, 20.0));
		firstSetT1.add(new Interval<Double>(50.0, 60.0));

		SortedSet<Interval<Double>> firstSetT2 = new TreeSet<Interval<Double>>();
		firstSetT2.add(new Interval<Double>(14.0, 22.0));
		firstSetT2.add(new Interval<Double>(45.0, 65.0));
		firstSet.add(firstSetT1);
		firstSet.add(firstSetT2);

		// second set

		ArrayList<SortedSet<Interval<Double>>> secondSet = new ArrayList<SortedSet<Interval<Double>>>(2);
		TreeSet<Interval<Double>> secondSetT1 = new TreeSet<Interval<Double>>();
		secondSetT1.add(new Interval<Double>(12.0, 13.0));
		secondSetT1.add(new Interval<Double>(80.0, 90.0));

		TreeSet<Interval<Double>> secondSetT2 = new TreeSet<Interval<Double>>();
		secondSetT2.add(new Interval<Double>(10.0, 15.0));
		secondSetT2.add(new Interval<Double>(60.0, 100.0));
		secondSet.add(secondSetT1);
		secondSet.add(secondSetT2);

		PowerPlantData pd1 = new PowerPlantData("AVPP1");
		pd1.setAllFeasibleRegions(firstSet);
		pd1.setPowerBoundaries(new Interval<Double>(10.0, 100.0));

		PowerPlantData pd2 = new PowerPlantData("AVPP2");
		pd2.setAllFeasibleRegions(secondSet);
		pd2.setPowerBoundaries(new Interval<Double>(5.0, 110.0));

		Collection<PowerPlantData> plants = new ArrayList<PowerPlantData>(2);
		plants.add(pd1);
		plants.add(pd2);
		TemporalAbstraction ta = new TemporalAbstraction();
		ta.setPowerPlants(plants);
		ta.perform(2);
		ta.printAll();

		Collection<Collection<Interval<Double>>> expecteds = new ArrayList<Collection<Interval<Double>>>(4);

		expecteds.add(PowerPlantFactory.getCollection(new double[] { 28.0, 33.0, 62.0, 73.0, 96.0, 110.0, 130.0, 150.0 }));
		expecteds.add(PowerPlantFactory.getCollection(new double[] { 24.0, 37.0, 55.0, 165.0 }));
		Assert.assertTrue(PowerPlantUtil.compareIntervalSets(expecteds, PowerPlantUtil.convert(ta.getAllFeasibleRegions())));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMergeListsOverlap() {
		// what happens if we have intervals [0,0] and [0 342.0]
		de.uniaugsburg.isse.abstraction.merging.MergeLists.List head = new de.uniaugsburg.isse.abstraction.merging.MergeLists.List(new Interval<Double>(0.0),
				null);
		Collection<Interval<Double>> otherRegions = Arrays.asList(new Interval<Double>(0.0, 342.0));
		head = PowerPlantUtil.plusSets(head, otherRegions);

		// should now just be one node with [0.0 342.0]
		Assert.assertEquals(new Interval<Double>(0.0, 342.0), head.getInterval());
		Assert.assertEquals(null, head.getNext());
	}

	/**
	 * Simple AVPP performing temporal abstraction with two different delta times
	 */
	@Test
	public void testDifferentDeltaTime() {
		// both plants between 0 and 100; 10 Units per 15 minutes can change
		PowerPlantData pd1 = PowerPlantFactory.getSimplePlant(0.0, 100.0, 90.0, 10.0, "CPP1");
		PowerPlantData pd2 = PowerPlantFactory.getSimplePlant(0.0, 100.0, 50.0, 10.0, "CPP2");
		Collection<PowerPlantData> pd = new ArrayList<PowerPlantData>(2);
		pd.add(pd1);
		pd.add(pd2);

		TemporalAbstraction ta = new TemporalAbstraction();
		ta.setPowerPlants(pd);
		ta.performNew();
		ta.perform(2); // 30 minutes - the first should reach its maximum, the second 70

		// step 1 : 1 -> [80, 100], 2 -> [40, 60] | combined : 120, 160
		// step 2 : 1 -> [70, 100], 2 -> [30, 70] | combined : 100, 170
		Assert.assertEquals(2, ta.getAllFeasibleRegions().size());

		Collection<Collection<Interval<Double>>> expecteds = new ArrayList<Collection<Interval<Double>>>(4);

		expecteds.add(PowerPlantFactory.getSingletonCollection(120.0, 160.0));
		expecteds.add(PowerPlantFactory.getSingletonCollection(100.0, 170.0));

		Assert.assertTrue(PowerPlantUtil.compareIntervalSets(expecteds, PowerPlantUtil.convert(ta.getAllFeasibleRegions())));

		// now with 3 minutes intervals -> 2 units change per time step
		ta = new TemporalAbstraction();
		ta.setPowerPlants(pd);
		ta.performNew();
		ta.setDeltaTime(3.0);
		ta.perform(2); // 10 minutes - the first should reach its maximum, the second 70

		Assert.assertEquals(2, ta.getAllFeasibleRegions().size());
		// step 1 : 1 -> [88, 92], 2 -> [48, 52] | combined : 136, 144
		// step 2 : 1 -> [86, 94], 2 -> [46, 54] | combined : 132, 148

		expecteds = new ArrayList<Collection<Interval<Double>>>(4);

		expecteds.add(PowerPlantFactory.getSingletonCollection(136.0, 144.0));
		expecteds.add(PowerPlantFactory.getSingletonCollection(132.0, 148.0));

		Assert.assertTrue(PowerPlantUtil.compareIntervalSets(expecteds, PowerPlantUtil.convert(ta.getAllFeasibleRegions())));
	}

	/**
	 * This test catches one problem occurred at runtime - an empty list in the set of feasible regions coming from a
	 * power plant underneath
	 */
	@Test
	public void testInfeasibleRegions() {
		HoleDetection hd = new HoleDetection();
		Collection<Collection<Interval<Double>>> plantIntervalsList = new ArrayList<Collection<Interval<Double>>>();
		Collection<Interval<Double>> firstIntervalList = new ArrayList<Interval<Double>>(1);
		Collection<Interval<Double>> secondIntervalList = new ArrayList<Interval<Double>>(0);

		firstIntervalList.add(new Interval<Double>(0.0, 500.0));
		plantIntervalsList.add(firstIntervalList);
		// empty list should not affect this
		plantIntervalsList.add(secondIntervalList);

		Collection<Interval<Double>> holes = hd.detectSupplyHolesNew(plantIntervalsList);
		SortedSet<Interval<Double>> feasibleRegions = hd.getIntervalList();

		Assert.assertNotNull(feasibleRegions);
		Assert.assertTrue(PowerPlantUtil.compareIntervals(firstIntervalList, feasibleRegions));
	}

	@Test
	public void testTwoAvppsDifferentHorizon() {
		ArrayList<SortedSet<Interval<Double>>> firstSet = new ArrayList<SortedSet<Interval<Double>>>(2);
		SortedSet<Interval<Double>> firstSetT1 = new TreeSet<Interval<Double>>();
		firstSetT1.add(new Interval<Double>(12.0, 15.0));

		SortedSet<Interval<Double>> firstSetT2 = new TreeSet<Interval<Double>>();
		firstSetT2.add(new Interval<Double>(10.0, 23.0));
		firstSet.add(firstSetT1);
		firstSet.add(firstSetT2);

		// second set

		ArrayList<SortedSet<Interval<Double>>> secondSet = new ArrayList<SortedSet<Interval<Double>>>(2);
		SortedSet<Interval<Double>> secondSetT1 = new TreeSet<Interval<Double>>();
		secondSetT1.add(new Interval<Double>(10.0, 20.0));

		SortedSet<Interval<Double>> secondSetT2 = new TreeSet<Interval<Double>>();
		secondSetT2.add(new Interval<Double>(9.0, 25.0));

		SortedSet<Interval<Double>> secondSetT3 = new TreeSet<Interval<Double>>();
		secondSetT3.add(new Interval<Double>(8.0, 27.0));

		secondSet.add(secondSetT1);
		secondSet.add(secondSetT2);
		secondSet.add(secondSetT3);

		PowerPlantData pd1 = new PowerPlantData("AVPP1");
		pd1.setAllFeasibleRegions(firstSet);
		pd1.setPowerBoundaries(new Interval<Double>(8.0, 25.0));
		TreeSet<Interval<Double>> feasReg1 = new TreeSet<Interval<Double>>();
		feasReg1.add(new Interval<Double>(8.0, 25.0));
		pd1.setFeasibleRegions(feasReg1);

		PowerPlantData pd2 = new PowerPlantData("AVPP2");
		pd2.setAllFeasibleRegions(secondSet);
		pd2.setPowerBoundaries(new Interval<Double>(7.0, 30.0));
		TreeSet<Interval<Double>> feasReg2 = new TreeSet<Interval<Double>>();
		feasReg2.add(new Interval<Double>(7.0, 30.0));
		pd2.setFeasibleRegions(feasReg2);

		Collection<PowerPlantData> plants = new ArrayList<PowerPlantData>(2);
		plants.add(pd1);
		plants.add(pd2);
		TemporalAbstraction ta = new TemporalAbstraction();
		ta.setPowerPlants(plants);
		ta.performNew();
		ta.perform(5);
		ta.printAll();

		Assert.assertEquals(4, ta.getAllFeasibleRegions().size());
		Collection<Collection<Interval<Double>>> expecteds = new ArrayList<Collection<Interval<Double>>>(4);

		expecteds.add(PowerPlantFactory.getCollection(new double[] { 22.0, 35.0 }));
		expecteds.add(PowerPlantFactory.getCollection(new double[] { 19.0, 48.0 }));
		expecteds.add(PowerPlantFactory.getCollection(new double[] { 16.0, 52.0 }));
		expecteds.add(PowerPlantFactory.getCollection(new double[] { 15.0, 55.0 }));

		Assert.assertTrue(PowerPlantUtil.compareIntervalSets(expecteds, PowerPlantUtil.convert(ta.getAllFeasibleRegions())));

	}

	/**
	 * Contains one AVPP and a Concrete PP
	 */
	@Test
	public void testMixedAvpp() {
		PowerPlantData pd1 = PowerPlantFactory.getOnPlant(10.0, 50.0, 30.0, 10.0, "CPP1");

		ArrayList<SortedSet<Interval<Double>>> firstSet = new ArrayList<SortedSet<Interval<Double>>>(2);
		SortedSet<Interval<Double>> firstSetT1 = new TreeSet<Interval<Double>>();
		firstSetT1.add(new Interval<Double>(10.0, 15.0));
		firstSetT1.add(new Interval<Double>(25.0, 30.0));

		SortedSet<Interval<Double>> firstSetT2 = new TreeSet<Interval<Double>>();
		firstSetT2.add(new Interval<Double>(8.0, 17.0));
		firstSetT2.add(new Interval<Double>(23.0, 32.0));
		firstSet.add(firstSetT1);
		firstSet.add(firstSetT2);

		PowerPlantData pd2 = new PowerPlantData("AVPP1");
		pd2.setAllFeasibleRegions(firstSet);
		TreeSet<Interval<Double>> generalFeasibleRegions = new TreeSet<Interval<Double>>();
		generalFeasibleRegions.add(new Interval<Double>(8.0, 17.0));
		generalFeasibleRegions.add(new Interval<Double>(23.0, 32.0));
		pd2.setFeasibleRegions(generalFeasibleRegions);
		pd2.setPowerBoundaries(new Interval<Double>(8.0, 32.0));
		Collection<PowerPlantData> pd = new ArrayList<PowerPlantData>(2);
		pd.add(pd1);
		pd.add(pd2);

		TemporalAbstraction ta = new TemporalAbstraction();
		ta.setPowerPlants(pd);
		ta.performNew();
		ta.perform(5);

		Assert.assertEquals(2, ta.getAllFeasibleRegions().size());
	}

	/**
	 * An AVPP that can be off after two time steps
	 */
	@Test
	public void testZeroable() {
		PowerPlantData pd1 = PowerPlantFactory.getSimplePlant(0.0, 100.0, 10.0, 10.0, "CPP1");
		PowerPlantData pd2 = PowerPlantFactory.getSimplePlant(0.0, 100.0, 25.0, 20.0, "CPP2");
		Collection<PowerPlantData> pd = new ArrayList<PowerPlantData>(2);
		pd.add(pd1);
		pd.add(pd2);

		TemporalAbstraction ta = new TemporalAbstraction();
		ta.setPowerPlants(pd);
		ta.performNew();
		ta.perform(2);

		Assert.assertEquals(2, ta.getAllFeasibleRegions().size());

		Collection<Collection<Interval<Double>>> expecteds = new ArrayList<Collection<Interval<Double>>>(4);

		expecteds.add(PowerPlantFactory.getSingletonCollection(5.0, 65.0));
		expecteds.add(PowerPlantFactory.getSingletonCollection(0.0, 95.0));

		Assert.assertTrue(PowerPlantUtil.compareIntervalSets(expecteds, PowerPlantUtil.convert(ta.getAllFeasibleRegions())));
	}

	/**
	 * Contains two concrete power plants having bounds, fixed change and forced on constraints
	 */
	@Test
	public void testConcretePlants() {
		PowerPlantData pd1 = PowerPlantFactory.getOnPlant(0.0, 100.0, 10.0, 10.0, "CPP1");
		PowerPlantData pd2 = PowerPlantFactory.getOnPlant(0.0, 100.0, 90, 20.0, "CPP2");
		Collection<PowerPlantData> pd = new ArrayList<PowerPlantData>(2);
		pd.add(pd1);
		pd.add(pd2);

		TemporalAbstraction ta = new TemporalAbstraction();
		ta.setPowerPlants(pd);
		ta.performNew();
		ta.perform(2);

		Assert.assertEquals(2, ta.getAllFeasibleRegions().size());

		Collection<Collection<Interval<Double>>> expecteds = new ArrayList<Collection<Interval<Double>>>(4);
		/*
		 * [70.0 120.0] , #### Feasible regions after t = 2 [50.0 130.0] ,
		 */
		expecteds.add(PowerPlantFactory.getSingletonCollection(70.0, 120.0));
		expecteds.add(PowerPlantFactory.getSingletonCollection(50.0, 130.0));

		Assert.assertTrue(PowerPlantUtil.compareIntervalSets(expecteds, PowerPlantUtil.convert(ta.getAllFeasibleRegions())));
	}

	/**
	 * Tests, if the state of convergence is reached after a number of time steps
	 */
	@Test
	public void testConvergence() {
		PowerPlantData pd1 = PowerPlantFactory.getOnPlant(10.0, 100.0, 30.0, 20.0, "CPP1");
		PowerPlantData pd2 = PowerPlantFactory.getOnPlant(20.0, 100.0, 40, 15.0, "CPP2");
		Collection<PowerPlantData> pd = new ArrayList<PowerPlantData>(2);
		pd.add(pd1);
		pd.add(pd2);

		// Test general abstraction here
		TemporalAbstraction ta = new TemporalAbstraction();
		ta.setPowerPlants(pd);
		ta.performNew();
		ta.perform(10);

		Assert.assertEquals(4, ta.getAllFeasibleRegions().size());

		Collection<Collection<Interval<Double>>> expecteds = new ArrayList<Collection<Interval<Double>>>(4);

		expecteds.add(PowerPlantFactory.getSingletonCollection(35.0, 105.0));
		expecteds.add(PowerPlantFactory.getSingletonCollection(30.0, 140.0));
		expecteds.add(PowerPlantFactory.getSingletonCollection(30.0, 175.0));
		expecteds.add(PowerPlantFactory.getSingletonCollection(30.0, 200.0));

		Assert.assertTrue(PowerPlantUtil.compareIntervalSets(expecteds, PowerPlantUtil.convert(ta.getAllFeasibleRegions())));
		/*
		 * 
		 * Collection<Interval<Double>> holes =ga.getHoles();
		 * Collection<Interval<Double>> feasibleRegions
		 * =ga.getFeasibleRegions();
		 * 
		 * // actuals Collection<Interval<Double>> expectedRegions = new
		 * ArrayList<Interval<Double>>(2); expectedRegions.add(new
		 * Interval<Double>(30.0, 200.0));
		 * 
		 * Assert.assertTrue(holes.isEmpty());
		 * Assert.assertTrue(PowerPlantFactory.compareIntervals(expectedRegions,
		 * feasibleRegions));
		 */
	}

	@Test
	public void testGeneralAbstraction() {
		PowerPlantData pd1 = PowerPlantFactory.getSimplePlant(24.0, 36.0, "CPP1");
		PowerPlantData pd2 = PowerPlantFactory.getSimplePlant(15.0, 20.0, "CPP2");
		PowerPlantData pd3 = PowerPlantFactory.getSimplePlant(20.0, 45.0, "CPP3");
		Collection<PowerPlantData> pd = new ArrayList<PowerPlantData>(3);
		pd.add(pd1);
		pd.add(pd2);
		pd.add(pd3);

		// Test general abstraction here
		GeneralAbstraction ga = new GeneralAbstraction();
		ga.setPowerPlants(pd);
		ga.perform();

		Collection<Interval<Double>> holes = ga.getHoles();
		Collection<Interval<Double>> feasibleRegions = ga.getFeasibleRegions();

		// actuals
		Collection<Interval<Double>> expectedHoles = new ArrayList<Interval<Double>>(1);
		expectedHoles.add(new Interval<Double>(0.0, 15.0));
		Collection<Interval<Double>> expectedRegions = new ArrayList<Interval<Double>>(2);
		expectedRegions.add(new Interval<Double>(0.0, 0.0));
		expectedRegions.add(new Interval<Double>(15.0, 101.0));

		Assert.assertTrue(PowerPlantUtil.compareIntervals(expectedHoles, holes));
		Assert.assertTrue(PowerPlantUtil.compareIntervals(expectedRegions, feasibleRegions));
	}

	@Test
	public void testMandatoryOnAbstraction() {
		PowerPlantData pd1 = PowerPlantFactory.getOnPlant(10.0, 100.0, "CPP1");
		PowerPlantData pd2 = PowerPlantFactory.getOnPlant(20.0, 100.0, "CPP2");
		Collection<PowerPlantData> pd = new ArrayList<PowerPlantData>(2);
		pd.add(pd1);
		pd.add(pd2);

		// Test general abstraction here
		GeneralAbstraction ga = new GeneralAbstraction();
		ga.setPowerPlants(pd);
		ga.performNew();

		Collection<Interval<Double>> holes = ga.getHoles();
		Collection<Interval<Double>> feasibleRegions = ga.getFeasibleRegions();

		// actuals
		Collection<Interval<Double>> expectedRegions = new ArrayList<Interval<Double>>(2);
		expectedRegions.add(new Interval<Double>(30.0, 200.0));

		Assert.assertTrue(holes.isEmpty());
		Assert.assertTrue(PowerPlantUtil.compareIntervals(expectedRegions, feasibleRegions));
	}

	/**
	 * PP1 [20 100] has to be on PP2 [10 100] can be off too
	 */
	@Test
	public void testMixedOnAbstraction() {
		PowerPlantData pd1 = PowerPlantFactory.getSimplePlant(10.0, 100.0, "CPP1");
		PowerPlantData pd2 = PowerPlantFactory.getOnPlant(20.0, 100.0, "CPP2");
		Collection<PowerPlantData> pd = new ArrayList<PowerPlantData>(2);
		pd.add(pd1);
		pd.add(pd2);

		// Test general abstraction here
		GeneralAbstraction ga = new GeneralAbstraction();
		ga.setPowerPlants(pd);
		ga.performNew();

		Collection<Interval<Double>> holes = ga.getHoles();
		Collection<Interval<Double>> feasibleRegions = ga.getFeasibleRegions();

		// actuals
		Collection<Interval<Double>> expectedRegions = new ArrayList<Interval<Double>>(2);
		expectedRegions.add(new Interval<Double>(20.0, 200.0));

		Assert.assertTrue(holes.isEmpty());
		Assert.assertTrue(PowerPlantUtil.compareIntervals(expectedRegions, feasibleRegions));
	}
}

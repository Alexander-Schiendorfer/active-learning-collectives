package de.uniaugsburg.isse.abstraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uniaugsburg.isse.abstraction.types.Interval;
import de.uniaugsburg.isse.abstraction.types.PiecewiseLinearFunction;
import de.uniaugsburg.isse.constraints.BoundsConstraint;
import de.uniaugsburg.isse.constraints.FixedChangeConstraint;
import de.uniaugsburg.isse.constraints.ForceOnConstraint;
import de.uniaugsburg.isse.constraints.GraduallyOffConstraint;
import de.uniaugsburg.isse.constraints.RateOfChangeConstraint;
import de.uniaugsburg.isse.constraints.StopTimeConstraint;
import de.uniaugsburg.isse.powerplants.PowerPlantData;
import de.uniaugsburg.isse.util.AbstractionParameterLiterals;
import de.uniaugsburg.isse.util.PowerPlantUtil;

public class AbstractionTest {
	private Collection<PowerPlantData> plantData;

	@Before
	public void setup() {
		plantData = new ArrayList<PowerPlantData>(3);

		// -- first sample plant
		PowerPlantData p1 = new PowerPlantData("P1");
		p1.setPowerBoundaries(new Interval<Double>(24.0, 36.0));

		p1.put("consRunningInit", "0");
		p1.put("consStoppingInit", "1");
		p1.put("minOffTime", "2");
		p1.put("minOnTime", "2");
		p1.put("rateOfChange", "0.15");
		p1.put("startupSlope", "1.0");
		p1.put("powerInit", "0.0");

		plantData.add(p1);

		// -- second sample plant
		PowerPlantData p2 = new PowerPlantData("P2");
		p2.setPowerBoundaries(new Interval<Double>(15.0, 20.0));
		p2.put("consRunningInit", "1");
		p2.put("consStoppingInit", "0");
		p2.put("minOffTime", "2");
		p2.put("minOnTime", "2");
		p2.put("rateOfChange", "0.125");
		p2.put("startupSlope", "1.0");
		p2.put("powerInit", "18.0");

		plantData.add(p2);

		// -- third sample plant
		PowerPlantData p3 = new PowerPlantData("P3");
		p3.setPowerBoundaries(new Interval<Double>(20.0, 45.0));
		p3.put("consRunningInit", "1");
		p3.put("consStoppingInit", "0");
		p3.put("minOffTime", "2");
		p3.put("minOnTime", "2");
		p3.put("rateOfChange", "0.2");
		p3.put("startupSlope", "1.0");
		p3.put("powerInit", "35.0");
		plantData.add(p3);

		// add constraints
		for (PowerPlantData pd : plantData) {
			RateOfChangeConstraint roc = new RateOfChangeConstraint(pd);
			pd.addConstraint(roc);

			BoundsConstraint bc = new BoundsConstraint(pd);
			pd.addConstraint(bc);

			GraduallyOffConstraint goc = new GraduallyOffConstraint(pd);
			pd.addConstraint(goc);

			StopTimeConstraint stc = new StopTimeConstraint(pd);
			pd.addConstraint(stc);
		}
	}

	@Test
	public void testGeneralAbstraction() {
		System.out.println("------------- General abstraction ------------");
		GeneralAbstraction ga = new GeneralAbstraction();
		ga.setPowerPlants(plantData);
		ga.perform();
		ga.print();

		System.out.println("Now for something completely different");
		ga.performNew();
		ga.print();
		System.out.println("------------- End general abstraction ------------");
	}

	@Test
	public void testTemporalAbstraction() {
		TemporalAbstraction ta = new TemporalAbstraction();
		ta.setPowerPlants(plantData);
		int T = 5; // up to T approximate
		ta.perform(T);
		ta.printAll();
	}

	@Test
	public void testMultiIntervalPlants() {
		SortedSet<Interval<Double>> firstPlantSet = new TreeSet<Interval<Double>>();
		SortedSet<Interval<Double>> secondPlantSet = new TreeSet<Interval<Double>>();
		firstPlantSet.add(new Interval<Double>(16.0, 20.0));
		firstPlantSet.add(new Interval<Double>(24.0, 30.0));

		secondPlantSet.add(new Interval<Double>(4.0, 7.0));
		secondPlantSet.add(new Interval<Double>(13.0, 15.0));

		PowerPlantData firstPlant = new PowerPlantData();
		firstPlant.setFeasibleRegions(firstPlantSet);
		firstPlant.setPowerBoundaries(new Interval<Double>(16.0, 30.0));

		PowerPlantData secondPlant = new PowerPlantData();
		secondPlant.setFeasibleRegions(secondPlantSet);
		secondPlant.setPowerBoundaries(new Interval<Double>(4.0, 15.0));

		Collection<PowerPlantData> plants = new ArrayList<PowerPlantData>(2);
		plants.add(firstPlant);
		plants.add(secondPlant);

		GeneralAbstraction ga = new GeneralAbstraction();
		ga.setPowerPlants(plants);
		ga.perform();

		for (Interval<Double> region : ga.getFeasibleRegions()) {
			System.out.println(region);
		}
	}

	@Test
	public void testConcreteExample() {

		PowerPlantData pd1 = new PowerPlantData("CPP1");
		pd1.setPowerBoundaries(new Interval<Double>(0.0, 100.0));
		pd1.put(AbstractionParameterLiterals.POWER_INIT, "10.0");
		pd1.put(AbstractionParameterLiterals.CONSRUNNING_INIT, "1");
		pd1.put(AbstractionParameterLiterals.CONSSTOPPING_INIT, "0");
		pd1.put(AbstractionParameterLiterals.MAX_PROD_CHANGE, "10.0");

		PowerPlantData pd2 = new PowerPlantData("CPP2");
		pd2.setPowerBoundaries(new Interval<Double>(0.0, 100.0));
		pd2.put(AbstractionParameterLiterals.POWER_INIT, "90.0");
		pd2.put(AbstractionParameterLiterals.CONSRUNNING_INIT, "1");
		pd2.put(AbstractionParameterLiterals.CONSSTOPPING_INIT, "0");
		pd2.put(AbstractionParameterLiterals.MAX_PROD_CHANGE, "20.0");

		Collection<PowerPlantData> plants = new ArrayList<PowerPlantData>(2);
		plants.add(pd1);
		plants.add(pd2);

		for (PowerPlantData pd : plants) {
			BoundsConstraint bc = new BoundsConstraint(pd);
			pd.addConstraint(bc);

			FixedChangeConstraint fcc = new FixedChangeConstraint(pd);
			pd.addConstraint(fcc);

			ForceOnConstraint foc = new ForceOnConstraint();
			pd.addConstraint(foc);
		}
		TemporalAbstraction ta = new TemporalAbstraction();
		ta.setPowerPlants(plants);
		ta.perform(5);
		ta.printAll();
	}

	@Test
	public void testThesisExample() {
		System.out.println("THESIS EXAMPLE");
		PowerPlantData pd1 = new PowerPlantData("a");
		pd1.setPowerBoundaries(new Interval<Double>(2., 5.));

		PowerPlantData pd2 = new PowerPlantData("b");
		pd2.setPowerBoundaries(new Interval<Double>(7., 10.0));

		PowerPlantData pd3 = new PowerPlantData("c");
		pd3.setPowerBoundaries(new Interval<Double>(25., 30.0));

		Collection<PowerPlantData> plants = new ArrayList<PowerPlantData>(3);
		plants.add(pd1);
		plants.add(pd2);
		plants.add(pd3);

		GeneralAbstraction ga = new GeneralAbstraction();
		ga.setPowerPlants(plants);
		ga.perform();
		ga.print();
	}

	@Test
	public void testTemporalAbstractionSingleton() {
		// only throw in one PowerPlantData object and perform temporal abstraction
		PowerPlantData singlePlant = new PowerPlantData("Singleton");
		singlePlant.setPowerBoundaries(new Interval<Double>(50., 100.));
		singlePlant.put(AbstractionParameterLiterals.CONSRUNNING_INIT, "1");
		singlePlant.put(AbstractionParameterLiterals.CONSSTOPPING_INIT, "0");
		singlePlant.put(AbstractionParameterLiterals.POWER_INIT, "70");
		singlePlant.put(AbstractionParameterLiterals.MAX_PROD_CHANGE, "10.0");
		singlePlant.addConstraint(new FixedChangeConstraint(singlePlant));
		singlePlant.addConstraint(new BoundsConstraint(singlePlant));
		singlePlant.addConstraint(new ForceOnConstraint());

		TemporalAbstraction ta = new TemporalAbstraction();
		Collection<PowerPlantData> powerPlants = new ArrayList<PowerPlantData>(1);
		powerPlants.add(singlePlant);
		ta.setPowerPlants(powerPlants);
		ta.perform(5);
		ta.printAll();
	}

	@Test
	public void testTemporalAbstractionPositiveDelta() {
		PowerPlantData firstPlant = PowerPlantUtil.getPowerPlantFixed("P1", 50, 100, 5);
		firstPlant.addConstraint(new ForceOnConstraint());
		firstPlant.put(AbstractionParameterLiterals.POWER_INIT, Double.toString(60));

		PowerPlantData secondPlant = PowerPlantUtil.getPowerPlantFixed("P2", 50, 100, 20);
		secondPlant.addConstraint(new ForceOnConstraint());
		secondPlant.put(AbstractionParameterLiterals.POWER_INIT, Double.toString(100));

		PowerPlantData thirdPlant = PowerPlantUtil.getPowerPlantFixed("P3", 50, 100, 20);
		thirdPlant.addConstraint(new ForceOnConstraint());
		thirdPlant.put(AbstractionParameterLiterals.POWER_INIT, Double.toString(90));

		// ------ now abstract
		TemporalAbstraction ta = new TemporalAbstraction();
		Collection<PowerPlantData> powerPlants = new ArrayList<PowerPlantData>(3);
		powerPlants.add(firstPlant);
		powerPlants.add(secondPlant);
		powerPlants.add(thirdPlant);

		ta.setPowerPlants(powerPlants);
		ta.perform(5);
		ta.printAll();

		// now get the positive delta
		PiecewiseLinearFunction pwl = ta.getMaxOutputFunctionByState(ta.getAllFeasibleRegions());
		double firstIn = pwl.getFirstInput();
		double firstOut = pwl.getFirstOutput();

		Assert.assertEquals(150.0, firstIn, 0.5);
		Assert.assertEquals(195.0, firstOut, 0.5);

		double[] inputs = new double[] { 150.0, 250.0, 265.0, 270.0, 275.0, 280.0 };
		double[] ouputs = new double[] { 195.0, 265.0, 270.0, 275.0, 280.0, 285.0 };

		Assert.assertArrayEquals(inputs, pwl.getIns(), 0.5);
		Assert.assertArrayEquals(ouputs, pwl.getOuts(), 0.5);

		// and the negative delta
		pwl = ta.getMinOutputFunctionByState(ta.getAllFeasibleRegions());
		inputs = new double[] { 160.0, 205.0, 250.0, 300.0 };
		ouputs = new double[] { 150.0, 160.0, 205.0, 255.0 };

		Assert.assertArrayEquals(inputs, pwl.getIns(), 0.5);
		Assert.assertArrayEquals(ouputs, pwl.getOuts(), 0.5);

	}

}

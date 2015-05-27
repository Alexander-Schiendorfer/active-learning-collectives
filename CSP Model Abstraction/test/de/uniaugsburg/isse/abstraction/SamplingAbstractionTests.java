package de.uniaugsburg.isse.abstraction;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import de.uniaugsburg.isse.ApproximateFunction;
import de.uniaugsburg.isse.RealMap;
import de.uniaugsburg.isse.abstraction.types.Interval;
import de.uniaugsburg.isse.abstraction.types.PiecewiseLinearFunction;
import de.uniaugsburg.isse.powerplants.PowerPlantData;
import de.uniaugsburg.isse.solver.AbstractModel;
import de.uniaugsburg.isse.solver.AbstractSolver;

public class SamplingAbstractionTests {

	private static class StubSolver extends AbstractSolver {

		private class StubModel implements AbstractModel {

			@Override
			public void addEqualityConstraint(String name, String decExpr, double value, double tolerance) {
				System.out.println("Constraint model setting " + decExpr + " = " + value + " (" + name + ")");
			}

			@Override
			public void setObjective(String decExpr, boolean maximize) {
				System.out.println("Setting objective to " + (maximize ? "maximize" : "minimize") + " " + decExpr);
			}

			@Override
			public Collection<PowerPlantData> getChildren() {
				return null;
			}

			@Override
			public PowerPlantData getAvpp() {
				return null;
			}

			@Override
			public void setPlantData(PowerPlantData avpp, Collection<PowerPlantData> children) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setTimeHorizon(int timeHorizon) {
			}

			@Override
			public int getTimeHorizon() {
				return 0;
			}

			@Override
			public void setFeasibleRegions(SortedSet<Interval<Double>> generalFeasibleRegions) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setGeneralHoles(Collection<Interval<Double>> generalHoles) {
			}

			@Override
			public Collection<Interval<Double>> getGeneralHoles() {
				return null;
			}

			@Override
			public void addDecisionExpressions(Collection<String> dexprs) {
			}

			@Override
			public void requireEqualBound() {
				// TODO Auto-generated method stub

			}

			@Override
			public void setInputExpression(String inputExpr) {
				// TODO Auto-generated method stub

			}

			@Override
			public void addInputLowerBoundConstraint(double lowerBound) {
				// TODO Auto-generated method stub

			}

			@Override
			public void addOutputLowerBound(String decExpr, double prevResult) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setCosts(boolean b) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setUseSoftConstraints(boolean useSoftConstraints) {
			}

			@Override
			public boolean isUseSoftConstraints() {
				return false;
			}

			@Override
			public void setCostsInCents(boolean b) {
				// TODO Auto-generated method stub

			}

		}

		public StubSolver() {
			this.setModel(new StubModel());
		}

		@Override
		public void solve() {
			System.out.println("Solving");
		}

		@Override
		public double getResult(String decExpr) {
			System.out.println("Returning result for " + decExpr);
			return 42;
		}

		@Override
		public void cleanup() {
			// TODO Auto-generated method stub

		}

		@Override
		public double getObjective() {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	@Test
	public void testInoutPairSemantics() {
		TreeSet<InOutPair> pairs = new TreeSet<InOutPair>();
		InOutPair iop = new InOutPair(10.0, 15.0);
		InOutPair iop2 = new InOutPair(10.02, 15.0);
		InOutPair iop3 = new InOutPair(10.0, 5.0);

		pairs.add(iop);
		pairs.add(iop2);
		pairs.add(iop3);

		Assert.assertEquals(2, pairs.size());
		Assert.assertTrue(pairs.contains(iop));
		Assert.assertTrue(pairs.contains(iop2));
	}

	@Test
	public void testTooLittleSamplePoints() {
		SortedSet<Interval<Double>> feasibleRegions = new TreeSet<Interval<Double>>();
		feasibleRegions.add(new Interval<Double>(1.0, 4.0));
		feasibleRegions.add(new Interval<Double>(6.0, 12.0));
		feasibleRegions.add(new Interval<Double>(14.0, 25.0));

		SamplingAbstraction sa = new SamplingAbstraction(feasibleRegions, null);
		sa.useStaticSampling = true;
		sa.setSolver(new StubSolver());
		sa.perform(0);

		List<Double> inputPoints = sa.getInputPoints();
		Assert.assertEquals(6, inputPoints.size());
		Double[] actuals = new Double[6];
		actuals = inputPoints.toArray(actuals);

		Double[] expecteds = new Double[] { 1.0, 4.0, 6.0, 12.0, 14.0, 25.0 };
		Assert.assertArrayEquals(expecteds, actuals);
	}

	@Test
	public void testPwLinearFunction() {
		double[] inputs = new double[] { 1.0, 2.0, 3.0, 4.0 };
		double[] outputs = new double[] { 4.0, 7.0, 5.0, 9.0 };
		PiecewiseLinearFunction pwlFunction = new PiecewiseLinearFunction();

		pwlFunction.convert(inputs, outputs);
		String s = pwlFunction.toCplex();
		System.out.println(s);
	}

	@Test
	public void testSimpleCostFunctionEvaluation() {
		double pricePerKwH = 5.0;

		PiecewiseLinearFunction costFunction = new PiecewiseLinearFunction(1.0, 10.0, pricePerKwH);
		costFunction.prolongAdInfinitum();

		for (double d = 0.0; d < 15.0; d += 5.0) {
			double expected = d * pricePerKwH;
			double actual = costFunction.evaluate(d);
			Assert.assertEquals(expected, actual, 0.001);
		}

	}

	@Test
	public void testPwLinearFunctionEvaluation() {
		ApproximateFunction af = new ApproximateFunction();
		PiecewiseLinearFunction pwlFunc = af.create(new RealMap() {

			@Override
			public double f(double x) {
				return x * x;
			}
		}, 1.0, 500.0, 4);

		System.out.println("int numBps=" + pwlFunc.getBPs() + ";\n");
		System.out.println("float breakpoints[1..numBps]=" + pwlFunc.getBreakpointString() + ";");
		System.out.println("float slopes[1..numBps+1]=" + pwlFunc.getSlopesString() + ";\n");

		System.out.println("pwlFunction errQuad = piecewise(i in 1..numBps) {");
		System.out.println("  slopes[i]->breakpoints[i];");
		System.out.println("  slopes[numBps+1]");
		System.out.println("} (" + pwlFunc.getFirstInput() + "," + pwlFunc.getFirstOutput() + ");");

		// now compare the outcomes of the pwlFunc at very specific points
		double[] fValues = new double[pwlFunc.getBPs()];
		double[] fEvaluates = new double[pwlFunc.getBPs()];

		for (int i = 0; i < pwlFunc.getBPs(); ++i) {
			fValues[i] = pwlFunc.evaluateBreakpoint(i);
			fEvaluates[i] = pwlFunc.evaluate(pwlFunc.getBreakpoints(pwlFunc.getBPs())[i]);
			System.out.println("Function at bp " + i + " = " + pwlFunc.getBreakpoints(pwlFunc.getBPs())[i] + " : " + fValues[i]);
		}

		double[] expecteds = new double[] { 1, 2.56, 6.5536, 16.777216 };
		Assert.assertArrayEquals(expecteds, fValues, 0.001);
		Assert.assertArrayEquals(expecteds, fEvaluates, 0.001);

		// two specific test points

		Assert.assertEquals(2.3, pwlFunc.evaluate(1.5), 0.001);
		Assert.assertEquals(16.777216, pwlFunc.evaluate(5), 0.001);
	}

	@Test
	public void testInfinitumPwLinearFunctionEvaluation() {
		ApproximateFunction af = new ApproximateFunction();
		PiecewiseLinearFunction pwlFunc = af.create(new RealMap() {

			@Override
			public double f(double x) {
				return x * x;
			}
		}, 1.0, 500.0, 4);
		pwlFunc.prolongAdInfinitum();

		System.out.println("int numBps=" + pwlFunc.getBPs() + ";\n");
		System.out.println("float breakpoints[1..numBps]=" + pwlFunc.getBreakpointString() + ";");
		System.out.println("float slopes[1..numBps+1]=" + pwlFunc.getSlopesString() + ";\n");

		System.out.println("pwlFunction errQuad = piecewise(i in 1..numBps) {");
		System.out.println("  slopes[i]->breakpoints[i];");
		System.out.println("  slopes[numBps+1]");
		System.out.println("} (" + pwlFunc.getFirstInput() + "," + pwlFunc.getFirstOutput() + ");");

		// now compare the outcomes of the pwlFunc at very specific points
		double[] fValues = new double[pwlFunc.getBPs()];
		double[] fEvaluates = new double[pwlFunc.getBPs()];

		for (int i = 0; i < pwlFunc.getBPs(); ++i) {
			fValues[i] = pwlFunc.evaluateBreakpoint(i);
			fEvaluates[i] = pwlFunc.evaluate(pwlFunc.getBreakpoints(pwlFunc.getBPs())[i]);
			System.out.println("Function at bp " + i + " = " + pwlFunc.getBreakpoints(pwlFunc.getBPs())[i] + " : " + fValues[i]);
		}

		double[] expecteds = new double[] { 1, 2.56, 6.5536, 16.777216 };
		Assert.assertArrayEquals(expecteds, fValues, 0.001);
		Assert.assertArrayEquals(expecteds, fEvaluates, 0.001);

		// two specific test points
		Assert.assertEquals(-0.3, pwlFunc.evaluate(0.5), 0.001);
		Assert.assertEquals(2.3, pwlFunc.evaluate(1.5), 0.001);
		Assert.assertEquals(22.79424, pwlFunc.evaluate(5), 0.001);
	}

	@Test
	public void simpleSamplingInputs() {
		// tests with a single feasible region [1, 10] and 5 sampling points
		/*
		 * np.linspace(1, 10, 5) Out[2]: array([ 1. , 3.25, 5.5 , 7.75, 10. ])
		 */
		SortedSet<Interval<Double>> feasibleRegions = new TreeSet<Interval<Double>>();
		feasibleRegions.add(new Interval<Double>(1.0, 10.0));

		SamplingAbstraction sa = new SamplingAbstraction(feasibleRegions, null);
		sa.useStaticSampling = true;
		sa.setSolver(new StubSolver());
		sa.perform(5);

		List<Double> inputs = sa.getInputPoints();
		Assert.assertEquals(5, inputs.size());

		Double[] expecteds = new Double[] { 1.0, 3.25, 5.5, 7.75, 10. };
		Double[] actuals = new Double[5];
		actuals = inputs.toArray(actuals);

		Assert.assertArrayEquals(expecteds, actuals);
	}

	@Test
	public void samplingMoreIntervalInputs() {
		// tests with a single feasible region [1, 10] and 5 sampling points
		/*
		 * np.linspace(1, 10, 5) Out[2]: array([ 1. , 3.25, 5.5 , 7.75, 10. ])
		 */
		SortedSet<Interval<Double>> feasibleRegions = new TreeSet<Interval<Double>>();
		feasibleRegions.add(new Interval<Double>(1.0, 4.0));
		feasibleRegions.add(new Interval<Double>(6.0, 10.0));
		feasibleRegions.add(new Interval<Double>(15.0, 35.0));

		SamplingAbstraction sa = new SamplingAbstraction(feasibleRegions, null);
		sa.useStaticSampling = true;
		sa.setSolver(new StubSolver());
		sa.perform(10);

		List<Double> inputs = sa.getInputPoints();
		// 4.0 coincides with one of the interval boundaries, otherwise 14
		Assert.assertEquals(13, inputs.size());

		Double[] expecteds = new Double[] { 1.0, 4.0, 6.0, 9.0, 10., 15., 17., 20., 23., 26., 29., 32., 35. };
		Double[] actuals = new Double[13];
		actuals = inputs.toArray(actuals);

		Assert.assertArrayEquals(expecteds, actuals);
	}

	@Test
	public void samplingJumpOverOneIntervalInputs() {
		// tests with a single feasible region [1, 10] and 5 sampling points
		/*
		 * np.linspace(1, 10, 5) Out[2]: array([ 1. , 3.25, 5.5 , 7.75, 10. ])
		 */
		SortedSet<Interval<Double>> feasibleRegions = new TreeSet<Interval<Double>>();
		feasibleRegions.add(new Interval<Double>(1.0, 4.0));
		feasibleRegions.add(new Interval<Double>(6.0, 8.0));
		feasibleRegions.add(new Interval<Double>(10.0, 40.0));

		SamplingAbstraction sa = new SamplingAbstraction(feasibleRegions, null);
		sa.useStaticSampling = true;
		sa.setSolver(new StubSolver());
		// effectively adding one sample point
		sa.perform(3);

		List<Double> inputs = sa.getInputPoints();
		// 4.0 coincides with one of the interval boundaries, otherwise 14
		Assert.assertEquals(7, inputs.size());

		Double[] expecteds = new Double[] { 1.0, 4.0, 6.0, 8.0, 10., 22.5, 40. };
		Double[] actuals = new Double[7];
		actuals = inputs.toArray(actuals);

		Assert.assertArrayEquals(expecteds, actuals);
	}

}

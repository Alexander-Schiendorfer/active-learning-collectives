package de.uniaugsburg.isse;

import java.util.ArrayList;
import java.util.Collection;

import de.uniaugsburg.isse.abstraction.types.Interval;
import de.uniaugsburg.isse.abstraction.types.PiecewiseLinearFunction;

public class ApproximateFunction {

	public boolean useExponential = true;

	/**
	 * This little util allows for approximation of an arbitrary \mathbb{R} \to \mathbb{R} function using piecewise
	 * linear functions in CPLEX;
	 * 
	 * take its output, throw it into sampleOplPwlApprox and see the magic
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ApproximateFunction af = new ApproximateFunction();

		PiecewiseLinearFunction pwlFunc = af.create(new RealMap() {

			@Override
			public double f(double x) {
				return x * x;
			}
		}, 1.0, 500.0, 15);

		pwlFunc.prolongAdInfinitum();
		System.out.println("int numBps=" + pwlFunc.getBPs() + ";\n");
		System.out.println("float breakpoints[1..numBps]=" + pwlFunc.getBreakpointString() + ";");
		System.out.println("float slopes[1..numBps+1]=" + pwlFunc.getSlopesString() + ";\n");

		System.out.println("pwlFunction errQuad = piecewise(i in 1..numBps) {");
		System.out.println("  slopes[i]->breakpoints[i];");
		System.out.println("  slopes[numBps+1]");
		System.out.println("} (" + pwlFunc.getFirstInput() + "," + pwlFunc.getFirstOutput() + ");");
	}

	/**
	 * Method to define a {@link PiecewiseLinearFunction} based on a given array of breakpoints for approximation.
	 * 
	 * @param function
	 * @param breakpoints
	 * @return
	 */
	private PiecewiseLinearFunction create(RealMap function, double[] breakpoints) {
		Collection<Interval<Double>> inOutPairs = new ArrayList<Interval<Double>>(breakpoints.length);

		for (double breakpoint : breakpoints) {
			double res = function.f(breakpoint);
			inOutPairs.add(new Interval<Double>(breakpoint, res));
		}

		PiecewiseLinearFunction pwlFunc = new PiecewiseLinearFunction();
		pwlFunc.convert(inOutPairs);
		return pwlFunc;
	}

	public PiecewiseLinearFunction create(RealMap function, double min, double max, int supportPoints) {
		double step = (max - min) / supportPoints;

		double current = min;
		Collection<Interval<Double>> inOutPairs = new ArrayList<Interval<Double>>(supportPoints + 1);

		if (useExponential) {
			while (supportPoints > 0) {
				double res = function.f(current);
				inOutPairs.add(new Interval<Double>(current, res));
				current *= 1.6;
				--supportPoints;
			}
		} else {
			while (current <= max) {
				double res = function.f(current);
				inOutPairs.add(new Interval<Double>(current, res));
				current += step;
			}
		}
		PiecewiseLinearFunction pwlFunc = new PiecewiseLinearFunction();
		pwlFunc.convert(inOutPairs);
		return pwlFunc;
	}

	/**
	 * Creates a {@link PiecewiseLinearFunction} approximating a quadratic function between min and max, using
	 * supportPoints count approximation points
	 * 
	 * @param min
	 * @param max
	 * @param supportPoints
	 * @return
	 */
	public static PiecewiseLinearFunction getQuadricPwl() {

		double breakPoints[] = { 62.5, 125, 250, 500, 1000 };
		ApproximateFunction af = new ApproximateFunction();
		PiecewiseLinearFunction pwlFunc = af.create(new RealMap() {

			@Override
			public double f(double x) {
				return x * x;
			}
		}, breakPoints);
		return pwlFunc;
	}
}

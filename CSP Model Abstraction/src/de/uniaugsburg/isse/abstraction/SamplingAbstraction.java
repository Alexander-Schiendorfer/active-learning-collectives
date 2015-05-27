package de.uniaugsburg.isse.abstraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uniaugsburg.isse.abstraction.selectors.PythonActiveLearningSelector;
import de.uniaugsburg.isse.abstraction.selectors.SamplingPointSelector;
import de.uniaugsburg.isse.abstraction.types.Interval;
import de.uniaugsburg.isse.abstraction.types.PiecewiseLinearFunction;
import de.uniaugsburg.isse.solver.AbstractSolver;
import de.uniaugsburg.isse.util.AbstractionParameterLiterals;

/**
 * Implements the sampling abstraction algorithm, with respect to functions with domain power
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class SamplingAbstraction {

	public static class OptimizationCriterion {

		public OptimizationCriterion(String decExpr, boolean minimize) {
			this.decExpr = decExpr;
			this.minimize = minimize;
		}

		public boolean minimize;
		public String decExpr;
		// x <= y => f(x) <= f(y) w.r. to some ordering <=
		private boolean monotonic;
		// x REL f(x) for REL \in {<=, >=}
		private boolean extensive;

		public void setMonotonic(boolean monotonic) {
			this.monotonic = monotonic;
		}

		@Override
		public int hashCode() {
			return this.decExpr.hashCode() * (minimize ? -1 : 1);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof OptimizationCriterion) {
				OptimizationCriterion oc = (OptimizationCriterion) obj;
				if (minimize == oc.minimize) {
					if ((decExpr == null && oc.decExpr == null) || (decExpr != null && oc.decExpr != null && decExpr.equals(oc.decExpr)))
						return true;
				}

				return false;
			}
			return false;
		}

		public String getIdentifier() {
			String prefix = minimize ? "min" : "max";
			return prefix + "_" + decExpr;
		}

		public boolean isMonotonic() {
			return this.monotonic;
		}

		public boolean isExtensive() {
			return this.extensive;
		}

		public void setExtensive(boolean extensive) {
			this.extensive = extensive;
		}

		@Override
		public String toString() {
			return getIdentifier();
		}
	}

	/**
	 * Model the resulting functions either having 0 slopes at the beginning and end or continue the neighboring slope
	 * 
	 * a) ------- / ----
	 * 
	 * b) / / /
	 */
	private boolean prolongAdInfinitum;
	protected SortedSet<Interval<Double>> generalFeasibleRegions;
	protected Collection<Interval<Double>> generalHoles;
	private List<Double> inputPoints;
	private AbstractSolver solver;
	private Collection<String> minimizationDecisionExpressions;
	private Collection<String> maximizationDecisionExpressions;
	private HashMap<OptimizationCriterion, PiecewiseLinearFunction> extractedFunctions;
	private double tolerance = 0; // if strictly greater 0 -> allow for
	protected SamplingPointSelector selector;
	protected boolean useStaticSampling = false;
	private int initialSamplingPoints = 0;

	public int getInitialSamplingPoints() {
		return initialSamplingPoints;
	}

	public void setInitialSamplingPoints(int initialSamplingPoints) {
		this.initialSamplingPoints = initialSamplingPoints;
	}

	public boolean isProlongAdInfinitum() {
		return prolongAdInfinitum;
	}

	public void setProlongAdInfinitum(boolean prolongAdInfinitum) {
		this.prolongAdInfinitum = prolongAdInfinitum;
	}

	public double getTolerance() {
		return tolerance;
	}

	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

	// tolerance to
	private double stepSize;

	public SamplingAbstraction() {
		minimizationDecisionExpressions = new LinkedList<String>();
		maximizationDecisionExpressions = new LinkedList<String>();
	}

	public SamplingAbstraction(SortedSet<Interval<Double>> generalFeasibleRegions, Collection<Interval<Double>> holes) {
		this();
		this.generalFeasibleRegions = generalFeasibleRegions;
		this.generalHoles = holes;
	}

	/**
	 * Performs sampling for domain power to target decision expression
	 * 
	 * @param decExpr
	 */
	public void perform(int samplePoints) {
		if (useStaticSampling) {
			setInputPoints(calculateInputPoints(samplePoints + initialSamplingPoints));
		} else {
			// selector = new EquidistantSelector(calculateInputPoints(samplePoints));
			// selector = new PythonEquidistantSelector(calculateInputPoints(samplePoints));
			selector = new PythonActiveLearningSelector(samplePoints);
		}

		if (solver == null || solver.getModel() == null)
			throw new RuntimeException("You forgot to set ConstraintSolver and/or ConstraintModel");

		// assuming AbstractModel and AbstractSolver are aready tied together
		// and consistent with generalFeasibleRegions
		solver.getModel().setFeasibleRegions(generalFeasibleRegions);
		solver.getModel().setGeneralHoles(generalHoles);

		Collection<OptimizationCriterion> optimizationCriteria = new ArrayList<SamplingAbstraction.OptimizationCriterion>(
				minimizationDecisionExpressions.size() + maximizationDecisionExpressions.size());

		// pdelta neg has to be extensive (x >= f(x))
		for (String minimization : minimizationDecisionExpressions) {
			OptimizationCriterion oc = new OptimizationCriterion(minimization, true);
			if (minimization.equals(AbstractionParameterLiterals.DEXP_COSTS + "Init")) {
				oc.setExtensive(false);
			} else
				oc.setExtensive(true);
			optimizationCriteria.add(oc);
		}

		// pdelta pos has to be extensive f(x) >= x
		for (String maximization : maximizationDecisionExpressions) {
			OptimizationCriterion oc = new OptimizationCriterion(maximization, false);
			// TODO let user decide whether problem is monotonic or not
			// Power management change speed is NOT monotonic, nor extensive in general
			// oc.setMonotonic(true);
			if (maximization.equals(AbstractionParameterLiterals.DEXP_COSTS + "Init")) {
				oc.setExtensive(false);
			} else
				oc.setExtensive(true);
			optimizationCriteria.add(oc);
		}

		extractedFunctions = new HashMap<OptimizationCriterion, PiecewiseLinearFunction>();

		for (OptimizationCriterion oc : optimizationCriteria) {
			getSolver().setObjective(oc.decExpr, oc.minimize);
			getSolver().setInputExpr(AbstractionParameterLiterals.DEXP_POWER + "Init");

			PiecewiseLinearFunction sampledFunction = sampleFunction(oc);

			extractedFunctions.put(oc, sampledFunction);
		}
	}

	/**
	 * Performs the actual sampling for a particular optimization criterion
	 * 
	 * @param oc
	 * @return
	 */
	private PiecewiseLinearFunction sampleFunction(OptimizationCriterion oc) {

		if (useStaticSampling)
			return sampleStatically(oc);
		else
			return sampleDynamically(oc);
	}

	private PiecewiseLinearFunction sampleDynamically(OptimizationCriterion oc) {
		// start with some initial sampling points
		List<Double> inputs = calculateInputPoints(initialSamplingPoints); // only the ones at boundaries
		TreeSet<InOutPair> sampledPairs = new TreeSet<InOutPair>(samplePoints(inputs, oc));

		selector.reset();
		selector.setInitialPoints(sampledPairs);
		selector.setAbstractionData(generalFeasibleRegions, generalHoles);

		while (selector.hasNext()) {
			double nextInput = selector.getNextInput();

			try {
				InOutPair pair = solve(nextInput, oc);

				if (pair.input != nextInput)
					System.out.println("Here some action may be required - I differ between actual and asked input");
				if (Math.abs(pair.input - nextInput) < 0.0001)
					pair.input = nextInput;

				selector.inform(pair);
				sampledPairs.add(pair);
			} catch (SamplingException se) {
				System.err.println("Could not find a solution for " + nextInput);
				selector.informFailure(nextInput);
			}
		}
		selector.destroy();
		PiecewiseLinearFunction pwlFunc = new PiecewiseLinearFunction();
		pwlFunc.convert(sampledPairs);
		return pwlFunc;
	}

	private List<InOutPair> samplePoints(List<Double> inputs, OptimizationCriterion oc) {
		List<InOutPair> pairs = new ArrayList<InOutPair>(inputs.size());
		for (double inp : inputs) {
			try {
				InOutPair pair = solve(inp, oc);
				pairs.add(pair);
			} catch (SamplingException e) {
				System.err.println("Could not find an output for input " + inp + " for oc " + oc);
			}
		}
		return pairs;
	}

	private PiecewiseLinearFunction sampleStatically(OptimizationCriterion oc) {

		List<Double> ins = getInputPoints();

		double[] inputs = new double[ins.size()];
		double[] outputs = new double[ins.size()];
		int index = 0;

		double prevResult = Double.NEGATIVE_INFINITY;
		double prevInput = Double.NEGATIVE_INFINITY;

		List<Double> inPoints = ins;
		for (Double inputPoint : inPoints) {
			getSolver().setInput(inputPoint, tolerance * stepSize);
			// make sure tolerance is limited to inputPoint - prevInput
			if (prevResult != Double.NEGATIVE_INFINITY) {
				double prevStep = inputPoint - prevInput;
				assert prevStep > 0.0;

				double inputLowerBound = Math.max(prevInput + prevStep * tolerance, inputPoint - prevStep * tolerance);
				getSolver().setInputLowerBound(inputLowerBound);
				if (oc.isMonotonic()) { // output has to be greater than or
										// equal to prev result

					getSolver().setOutputLowerBound(oc.decExpr, prevResult);
					// TODO revise monotonicity for minimization problems
				}
			}
			if (oc.isExtensive()) {
				// x <= y => f(x) <= f(y) && f(x) >= x for maximization
				getSolver().getModel().requireEqualBound();
			}
			getSolver().solve();

			if (getSolver().isSolved()) {
				// actually I would prefer oc.decExpr to state the decision expression
				// but there is a bug in CPLEX (12.4) prohibiting this -> thus use objective
				// double result = getSolver().getResult(oc.decExpr);
				double result = getSolver().getObjective();

				double actualInput = getSolver().getResult(AbstractionParameterLiterals.DEXP_POWER + "Init");

				inputs[index] = actualInput;
				outputs[index] = result;

				double jit = 0.001;

				if (!oc.minimize && result < prevResult && prevResult - result > jit) {

					System.out.println("Res: " + result + " in " + actualInput);
					System.out.println("PrevRes: " + prevResult + " in " + prevInput);
				}

				if (result < prevResult && prevResult - result <= jit) {
					result = prevResult;
					outputs[index] = result;
				}

				if (Math.abs(actualInput - result) < jit) {
					result = actualInput;
					outputs[index] = result;
				}

				if ((actualInput < result && oc.minimize && oc.extensive) || (actualInput > result && !oc.minimize && oc.extensive)) {
					throw new RuntimeException("Invalid result input: " + actualInput + " output " + result + " violates extensivity property ");
				}
				++index;
				getSolver().cleanup();
				prevInput = actualInput;
				prevResult = result;
			} else {
				getSolver().cleanup();
				System.out.println("Failed to find a solution for " + oc.decExpr + " " + oc.minimize);

				// Workaround for extensive objectives (then diagonal might be cut by missing point and 0 slope)
				if (oc.isExtensive()) {
					// add at least equality
					inputs[index] = inputPoint;
					outputs[index] = inputPoint;
					++index;
				}
			} // solver found a solution ?
		} // for inputs

		if (index == 0)
			throw new RuntimeException("Empty points received!" + Arrays.toString(inPoints.toArray()));

		PiecewiseLinearFunction pwl = new PiecewiseLinearFunction();
		pwl.convert(inputs, outputs, index);
		if (prolongAdInfinitum)
			pwl.prolongAdInfinitum();
		return pwl;

	}

	/**
	 * Calculates a list of equi-distant sample points contains minimally the interval boundaries of feasible regions
	 * 
	 * @param samplePoints
	 * @return
	 */
	private List<Double> calculateInputPoints(int samplePoints) {
		if (generalFeasibleRegions == null || generalFeasibleRegions.isEmpty())
			return null;

		TreeSet<Double> samples = new TreeSet<Double>();
		double totalDomainRange = 0.0;

		// boundary values of feasible region intervals will *always* be added
		for (Interval<Double> feasibleRegion : generalFeasibleRegions) {
			samples.add(feasibleRegion.min);
			samples.add(feasibleRegion.max);
			totalDomainRange += (feasibleRegion.max - feasibleRegion.min);
		}

		// combine remaining domain input to one long interval, divide by
		// remaining points and add those
		// equi-distant steps

		stepSize = totalDomainRange / (samplePoints - 1);
		double currentInput = generalFeasibleRegions.first().min;
		Iterator<Interval<Double>> feasibleRegionIterator = generalFeasibleRegions.iterator();
		// there has to be at least one - thus unchecked
		Interval<Double> currentRegion = feasibleRegionIterator.next();

		while (samplePoints - 2 > 0) {
			double nextInput = currentInput + stepSize;

			while (nextInput > currentRegion.max) {
				double nextOffset = nextInput - currentRegion.max;
				if (feasibleRegionIterator.hasNext()) {
					currentRegion = feasibleRegionIterator.next();
					nextInput = currentRegion.min + nextOffset;
				} else {
					System.err.println("WARNING: I want to do " + samplePoints + " more steps, but reached already " + nextInput + " whereas " + currentRegion
							+ " is my last region");
				}
			}

			currentInput = nextInput;
			samples.add(currentInput);
			--samplePoints;

		}
		List<Double> sortedInputs = new ArrayList<Double>(samples.size());
		sortedInputs.addAll(samples);
		return sortedInputs;
	}

	public List<Double> getInputPoints() {
		return inputPoints;
	}

	public void setInputPoints(List<Double> inputPoints) {
		this.inputPoints = inputPoints;
	}

	public AbstractSolver getSolver() {
		return solver;
	}

	public void setSolver(AbstractSolver solver) {
		this.solver = solver;
	}

	public Collection<String> getMinimizationDecisionExpressions() {
		return minimizationDecisionExpressions;
	}

	public void setMinimizationDecisionExpressions(Collection<String> minimizationDecisionExpressions) {
		this.minimizationDecisionExpressions = minimizationDecisionExpressions;
	}

	public Collection<String> getMaximizationDecisionExpressions() {
		return maximizationDecisionExpressions;
	}

	public void setMaximizationDecisionExpressions(Collection<String> maximizationDecisionExpressions) {
		this.maximizationDecisionExpressions = maximizationDecisionExpressions;
	}

	public PiecewiseLinearFunction getPiecewiseLinearFunction(String decExpr, boolean minimize) {
		return extractedFunctions.get(new OptimizationCriterion(decExpr, minimize));
	}

	public SortedSet<Interval<Double>> getGeneralFeasibleRegions() {
		return generalFeasibleRegions;
	}

	public void setGeneralFeasibleRegions(SortedSet<Interval<Double>> generalFeasibleRegions) {
		this.generalFeasibleRegions = generalFeasibleRegions;
	}

	public Collection<Interval<Double>> getGeneralHoles() {
		return generalHoles;
	}

	public void setGeneralHoles(Collection<Interval<Double>> generalHoles) {
		this.generalHoles = generalHoles;
	}

	public SamplingPointSelector getSelector() {
		return selector;
	}

	public void setSelector(SamplingPointSelector selector) {
		this.selector = selector;
	}

	public boolean isUseStaticSampling() {
		return useStaticSampling;
	}

	public void setUseStaticSampling(boolean useStaticSampling) {
		this.useStaticSampling = useStaticSampling;
	}

	public InOutPair solve(double input, OptimizationCriterion oc) throws SamplingException {
		getSolver().setInput(input, tolerance * stepSize);
		if (oc.isExtensive()) {
			// x <= y => f(x) <= f(y) && f(x) >= x for maximization
			getSolver().getModel().requireEqualBound();
		}
		getSolver().solve();
		if (getSolver().isSolved()) {
			double result = getSolver().getObjective();
			double actualInput = getSolver().getResult(AbstractionParameterLiterals.DEXP_POWER + "Init");
			getSolver().cleanup();
			return new InOutPair(actualInput, result);
		} else {
			getSolver().cleanup(); // try once more without presolve

			throw new SamplingException("Could not solve for input " + input + " for OC " + oc.toString());
		}
	}
}

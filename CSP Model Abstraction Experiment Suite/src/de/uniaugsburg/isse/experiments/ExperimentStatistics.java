package de.uniaugsburg.isse.experiments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * All temporal measurements in nanoseconds
 * 
 * @author alexander
 * 
 */
public class ExperimentStatistics {

	private static class MovingAverage {
		private double min;
		private double max;
		private double avg;
		private double sum;
		private long num;
		private List<Double> rawData;
		private List<Double> allRawData; // over multiple run
		private String name;

		public MovingAverage() {
			min = Double.POSITIVE_INFINITY;
			max = Double.NEGATIVE_INFINITY;
			avg = sum = num = 0;
			rawData = new ArrayList<Double>(200);
			allRawData = new ArrayList<Double>(1000);
		}

		public void reset() {
			allRawData.addAll(rawData);
			min = Double.POSITIVE_INFINITY;
			max = Double.NEGATIVE_INFINITY;
			avg = sum = num = 0;
			rawData.clear();
		}

		public MovingAverage(String name) {
			this();
			this.name = name;
		}

		public void add(double value) {
			min = Math.min(min, value);
			max = Math.max(max, value);
			sum = (avg * num) + value;
			avg = sum / (num + 1);
			++num;
			rawData.add(value);
		}

		public String getName() {
			return name;
		}

		public double getVar() {
			double sumVar = 0.0;
			for (int i = 0; i < rawData.size(); ++i) {
				double diff = rawData.get(i) - avg;
				sumVar += diff * diff;
			}
			return sumVar / (rawData.size() - 1);
		}

	}

	private long abstractionRuntime;
	private long fixedAbstractionRuntime;

	public long getFixedAbstractionRuntime() {
		return fixedAbstractionRuntime;
	}

	public void setFixedAbstractionRuntime(long fixedAbstractionRuntime) {
		this.fixedAbstractionRuntime = fixedAbstractionRuntime;
	}

	private long longestSerialPath;
	private boolean solveCentrally = true;
	private boolean solveHierarchically = true;
	private MovingAverage abstractionError;
	private MovingAverage topLevelViolationAbstracted;
	private MovingAverage topLevelViolationActualCentral;
	private MovingAverage topLevelViolationActualRegioCentral;
	private MovingAverage relativeTLViolationRegioCentral;
	private MovingAverage relativeTLViolationCentral;

	private MovingAverage topLevelViolationAbstractedRel;
	private MovingAverage abstractionErrorRel;
	private MovingAverage variableAbstraction;
	private MovingAverage runtimePerStepCentral;
	private MovingAverage runtimePerStepRegioCentral;
	private MovingAverage runtimePerAvppPerStep;
	private MovingAverage unsolvedAllocations; // should be 0 now

	private Collection<MovingAverage> averages;
	private MovingAverage runtimeCentral;
	private MovingAverage runtimeRegioCentral;
	private MovingAverage timeStamps; // just for protocols
	private MovingAverage topLevelCostsRegioCentral;
	private MovingAverage topLevelCostsCentral;
	private MovingAverage rootCostsRegioCentral;
	private MovingAverage longestSerialPaths;
	private MovingAverage fixedAbstractionTimes;
	private MovingAverage fixedAbstractionTimesPerAVPP;

	public ExperimentStatistics() {
		averages = new ArrayList<MovingAverage>(20);
		abstractionError = new MovingAverage("AbstractionError");
		averages.add(abstractionError);

		topLevelViolationAbstracted = new MovingAverage("TopLevelViolation Abstracted");
		averages.add(topLevelViolationAbstracted);

		topLevelViolationActualCentral = new MovingAverage("topLevelViolationActualCentral");
		averages.add(topLevelViolationActualCentral);

		topLevelViolationActualRegioCentral = new MovingAverage("topLevelViolationActualRegioCentral");
		averages.add(topLevelViolationActualRegioCentral);

		relativeTLViolationCentral = new MovingAverage("relativeTLViolationCentral");
		averages.add(relativeTLViolationCentral);

		relativeTLViolationRegioCentral = new MovingAverage("relativeTLViolationRegioCentral");
		averages.add(relativeTLViolationRegioCentral);

		topLevelViolationAbstractedRel = new MovingAverage("topLevelViolationAbstractedRel");
		averages.add(topLevelViolationAbstractedRel);

		abstractionErrorRel = new MovingAverage("abstractionErrorRel");
		averages.add(abstractionErrorRel);

		variableAbstraction = new MovingAverage("variableAbstraction");
		averages.add(variableAbstraction);

		runtimePerStepCentral = new MovingAverage("runtimePerStepCentral");
		averages.add(runtimePerStepCentral);

		runtimePerStepRegioCentral = new MovingAverage("runtimePerStepRegioCentral");
		averages.add(runtimePerStepRegioCentral);

		runtimePerAvppPerStep = new MovingAverage("runtimePerAvppPerStep");
		averages.add(runtimePerAvppPerStep);

		unsolvedAllocations = new MovingAverage("unsolvedCentralAllocations");
		averages.add(unsolvedAllocations);

		runtimeCentral = new MovingAverage("Totalruntimecentral");
		averages.add(runtimeCentral);

		runtimeRegioCentral = new MovingAverage("Totalruntimeregiocentral");
		averages.add(runtimeRegioCentral);

		timeStamps = new MovingAverage("timestamps");
		averages.add(timeStamps);

		topLevelCostsCentral = new MovingAverage("Toplevelcostscentral");
		averages.add(topLevelCostsCentral);

		topLevelCostsRegioCentral = new MovingAverage("Toplevelcostsregiocentral");
		averages.add(topLevelCostsRegioCentral);

		rootCostsRegioCentral = new MovingAverage("Rootcostsregiocentral");
		averages.add(rootCostsRegioCentral);

		longestSerialPaths = new MovingAverage("Longest serial path");
		averages.add(longestSerialPaths);

		fixedAbstractionTimes = new MovingAverage("FixedAbstractionTimes");
		averages.add(fixedAbstractionTimes);

		fixedAbstractionTimesPerAVPP = new MovingAverage("FixedAbstractionTimesPerAVPP");
		averages.add(fixedAbstractionTimesPerAVPP);

	}

	public void reset() {
		for (MovingAverage avg : averages) {
			avg.reset();
		}
	}

	public void addTimeStamp(long stamp) {
		timeStamps.add(stamp);
	}

	public void addTotalRuntimeCentral(long elapsed) {
		runtimeCentral.add(toSeconds(elapsed));
	}

	public void addTotalRuntimeRegioCentral(long elapsed) {
		runtimeRegioCentral.add(toSeconds(elapsed));
	}

	public long getAbstractionRuntime() {
		return abstractionRuntime;
	}

	public void addAbstractionRuntime(long abstractionRuntime) {
		this.abstractionRuntime = abstractionRuntime;
		fixedAbstractionTimes.add(toSeconds(abstractionRuntime));
	}

	public long getLongestSerialPath() {
		return longestSerialPath;
	}

	public void setLongestSerialPath(long longestSerialPath) {
		this.longestSerialPath = longestSerialPath;
	}

	public void reportSerialPath(long l) {
		longestSerialPath = Math.round(Math.max(toSeconds(l), longestSerialPath));
		longestSerialPaths.add(toSeconds(l));
	}

	public void reportUnsolvedAllocation(double runSucces) {
		unsolvedAllocations.add(runSucces);
	}

	public void reportAbstractionError(String topLevelAppIdent, Map<String, Double> loadInputs, Map<String, Double> actualLoads) {
		for (Entry<String, Double> entry : loadInputs.entrySet()) {
			Double actualLoad = actualLoads.get(entry.getKey());
			Double desiredLoad = entry.getValue();

			double violation = Math.abs(desiredLoad - actualLoad);
			double violationRel = violation / desiredLoad;
			if (entry.getKey().equals(topLevelAppIdent)) {
				topLevelViolationAbstracted.add(violation);
				if (desiredLoad > 0)
					topLevelViolationAbstractedRel.add(violationRel);
			} else {
				abstractionError.add(violation);
				if (desiredLoad > 0)
					abstractionErrorRel.add(violationRel);
			}
		}

	}

	public String writeCsv() {
		StringBuilder sb = new StringBuilder();
		// title line
		Collection<String> strings = new ArrayList<String>(averages.size());
		int maxLen = Integer.MIN_VALUE;
		for (MovingAverage avg : averages) {
			if (!avg.allRawData.isEmpty()) {
				strings.add(avg.getName());
				maxLen = Math.max(maxLen, avg.allRawData.size());
			}
		}

		sb.append(de.uniaugsburg.isse.util.Utils.getSeparatedListOfStrings(strings, ";") + "\n");
		for (int line = 0; line < maxLen; ++line) {
			strings.clear();
			for (MovingAverage avg : averages) {
				if (!avg.allRawData.isEmpty()) {
					if (line < avg.allRawData.size()) {
						strings.add(Double.toString(avg.allRawData.get(line)));
					} else {
						strings.add("");
					}
				}
			}
			sb.append(de.uniaugsburg.isse.util.Utils.getSeparatedListOfStrings(strings, ";"));
			sb.append("\n");
		}

		// some aggregate statistics
		return sb.toString();
	}

	public String writeStatistics() {
		StringBuilder sb = new StringBuilder("+++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");

		if (solveCentrally) {
			appendMovingAverage(sb, "Runtime central ", runtimeCentral);
			appendMovingAverage(sb, "Runtime central per time step ", runtimePerStepCentral);
			appendMovingAverage(sb, "Violation total central avg per timestep", topLevelViolationActualCentral);
			appendMovingAverage(sb, "Relative Violation total central avg per timestep", relativeTLViolationCentral);
			appendMovingAverage(sb, "Costs total central", topLevelCostsCentral);
		}

		if (solveHierarchically) {
			appendMovingAverage(sb, "Runtime regio central ", runtimeRegioCentral);

			appendMovingAverage(sb, "Runtime regio central per time step ", runtimePerStepRegioCentral);
			appendMovingAverage(sb, "Violation total regio central avg per timestep", topLevelViolationActualRegioCentral);
			appendMovingAverage(sb, "Costs total regio central", topLevelCostsRegioCentral);
			appendMovingAverage(sb, "Costs root level regio central", rootCostsRegioCentral);

			appendMovingAverage(sb, "Relative Violation total regio central avg per timestep", relativeTLViolationRegioCentral);
			appendMovingAverage(sb, "Abstraction error", abstractionError);
			appendMovingAverage(sb, "Relative Abstraction error", abstractionErrorRel);
			sb.append("+ Longest serial path : " + toSeconds(longestSerialPath) + "\n");
			appendMovingAverage(sb, " - Longest serial paths ", longestSerialPaths);
			appendMovingAverage(sb, "Time per AVPP solving", runtimePerAvppPerStep);
			sb.append("+ Time spent in abstraction: " + toSeconds(abstractionRuntime) + "\n");
			sb.append("+ Fixed time abstraction: " + toSeconds(fixedAbstractionRuntime) + "\n");
			sb.append("+ Variable abstraction runtime total : " + Math.round(variableAbstraction.sum) + "\n");
			appendMovingAverage(sb, "Variable abstraction runtime per step ", variableAbstraction);
		}
		sb.append("+++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		return sb.toString();
	}

	public static double toSeconds(long nanoseconds) {
		return nanoseconds * 1.0e-9;
	}

	private void appendMovingAverage(StringBuilder sb, String fieldName, MovingAverage movingAverage) {
		sb.append("+ " + fieldName + "\n");
		sb.append("++ min " + movingAverage.min + "\n");
		sb.append("++ max " + movingAverage.max + "\n");
		sb.append("++ avg " + movingAverage.avg + "\n");
		sb.append("++ sum " + movingAverage.sum + "\n");
		sb.append("++ n " + movingAverage.num + "\n");
		double var = movingAverage.getVar();
		sb.append("++ var " + var + "\n");
		sb.append("++ stddev " + Math.sqrt(var) + "\n");
	}

	public void addToplevelViolationCentral(double totalProduction, double residualLoad) {
		double violation = Math.abs(residualLoad - totalProduction);
		topLevelViolationActualCentral.add(violation);
		double relViolation = violation / (residualLoad + 0.001);
		relativeTLViolationCentral.add(relViolation);
	}

	public void addToplevelViolationRegioCentral(double totalProduction, double residualLoad) {
		double violation = Math.abs(residualLoad - totalProduction);
		double relViolation = violation / residualLoad;
		topLevelViolationActualRegioCentral.add(violation);
		relativeTLViolationRegioCentral.add(relViolation);
	}

	public void addVariableAbstractionTime(long elapsed) {
		variableAbstraction.add(toSeconds(elapsed));
	}

	public void addCentralRuntimePerStep(long elapsed) {
		runtimePerStepCentral.add(toSeconds(elapsed));
	}

	public void addRegioCentralRuntimePerStep(long elapsed) {
		runtimePerStepRegioCentral.add(toSeconds(elapsed));
	}

	public void addAvppRuntime(long elapsed) {
		runtimePerAvppPerStep.add(toSeconds(elapsed));
	}

	public void addToplevelCostsRegioCentral(double totalCosts) {
		topLevelCostsRegioCentral.add(totalCosts);
	}

	public void addToplevelCostsCentral(double totalCosts) {
		topLevelCostsCentral.add(totalCosts);
	}

	public void addRootCostsRegioCentral(double totalCosts) {
		rootCostsRegioCentral.add(totalCosts);
	}

	public boolean isSolveCentrally() {
		return solveCentrally;
	}

	public void setSolveCentrally(boolean solveCentrally) {
		this.solveCentrally = solveCentrally;
	}

	public boolean isSolveHierarchically() {
		return solveHierarchically;
	}

	public void setSolveHierarchically(boolean solveHierarchically) {
		this.solveHierarchically = solveHierarchically;
	}

	public void addAvppAbstractionRuntime(long elapsed) {
		this.fixedAbstractionTimesPerAVPP.add(elapsed);
	}
}

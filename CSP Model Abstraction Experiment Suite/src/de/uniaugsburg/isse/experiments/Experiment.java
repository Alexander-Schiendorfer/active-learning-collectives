package de.uniaugsburg.isse.experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import optimizationtools.CplexTools;
import de.uniaugsburg.isse.RandomManager;
import de.uniaugsburg.isse.abstraction.AvppCreator;
import de.uniaugsburg.isse.abstraction.AvppGraph;
import de.uniaugsburg.isse.abstraction.AvppLeafNode;
import de.uniaugsburg.isse.abstraction.CplexAvppGraphExporter;
import de.uniaugsburg.isse.abstraction.CplexExporter;
import de.uniaugsburg.isse.abstraction.GeneralAbstraction;
import de.uniaugsburg.isse.abstraction.SamplingAbstraction;
import de.uniaugsburg.isse.abstraction.TemporalAbstraction;
import de.uniaugsburg.isse.abstraction.types.Interval;
import de.uniaugsburg.isse.abstraction.types.PiecewiseLinearFunction;
import de.uniaugsburg.isse.data.ResidualLoadReader;
import de.uniaugsburg.isse.experiments.ExperimentParameterLiterals.PowerplantType;
import de.uniaugsburg.isse.powerplants.FilePowerPlantSource;
import de.uniaugsburg.isse.powerplants.PowerPlantData;
import de.uniaugsburg.isse.powerplants.PowerPlantSource;
import de.uniaugsburg.isse.powerplants.PowerPlantState;
import de.uniaugsburg.isse.solver.AbstractModel;
import de.uniaugsburg.isse.solver.AbstractSolver;
import de.uniaugsburg.isse.solver.AbstractSolverFactory;
import de.uniaugsburg.isse.solver.SolverFacade;
import de.uniaugsburg.isse.timer.Timer;
import de.uniaugsburg.isse.timer.TimerCategory;
import de.uniaugsburg.isse.util.AbstractionParameterLiterals;
import de.uniaugsburg.isse.util.Utils;

/**
 * This class represents one particular experiment of scheduling an AVPP (the top level AVPP) - can be used to set the
 * balancing level; After an AVPP is formed, new P_min and P_max level for each concrete plant are found by looking at
 * the maximal and minimal residual load to meed
 * 
 * Different possible experiments include
 * 
 * -> Sum(P_Max) >= Max_Residual_Load && Sum(P_min) <= Min_Residual_Load i.e. all residual loads can in principle be met
 * by the AVPP; this should be achieved by selecting P_max values with µ = Max(Load) / #plants
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class Experiment {

	// ======================================
	// Experiment meta data
	// ======================================
	protected int timeHorizon;
	protected int experimentHorizon;
	protected int samplingPoints = 10;
	protected Double[] residualLoad;
	protected long HierarchyRandomSeed = 1337;
	protected long AvppsRandomSeed = 1337;
	protected long InitialStatesSeed = 1337;
	protected HierarchyType hierarchyType;
	protected boolean disconnectable; // turn on/off on
	protected boolean useSamplingAbstraction;
	protected boolean useTemporalAbstraction = true;
	protected boolean solveCentrally = true; // to switch central solution in case of several equal runs (e.g., sampling
												// points)
	protected boolean solveHierarchically = true;
	protected boolean useStaticSampling = true;
	protected int minutesPerTimestep = 15;
	protected boolean detailedInertia = false;
	protected boolean useCostsInCents = false;

	/**
	 * This one is to be used if the number of plants is directly specified by the input file
	 */
	protected ExperimentParameterLiterals.NumberPlants numberOfPlants;
	/**
	 * This one holds if we draw from a random distribution; specify how many plants to take
	 */
	protected int countPlants;
	/**
	 * These two are necessary to parametrize the avpp creator
	 */
	protected int plantsPerAvpp;
	protected int avppsPerAvpp;

	// ======================================
	// AVPP graph, states and power plant
	// data collections
	// ======================================
	protected PowerPlantSource source;
	protected AvppGraph avppGraph;
	protected Map<String, PowerPlantState> allStates;
	protected Map<String, PowerPlantState> concretePlantStates;
	protected Map<String, PowerPlantState> initialStates;
	protected List<PowerPlantData> allPlants;
	protected double maxProduction;

	protected SolverFacade solverFacade;
	protected AbstractSolverFactory solverFactory;

	// Evaluation
	protected StringBuilder violationBookmarkBuilder;
	protected Timer timer;
	protected ExperimentStatistics statistics;

	protected Map<String, Double> loadInputs; // desired target
	protected Map<String, Double> actualLoads; // actual produced load

	protected double maxLoad;
	protected double minLoad;
	protected final double jitter = 0.001; // matches CPLEX file

	protected Properties originatingProperties; // all parameters set here

	protected long experimentStartTime;
	private long currentLongestSerialPath;
	private boolean useFeaturesAlgorithm;
	private AlgorithmFeatures features;
	private int initialSamplingPoints;

	public Properties getOriginatingProperties() {
		return originatingProperties;
	}

	public void setOriginatingProperties(Properties originatingProperties) {
		this.originatingProperties = originatingProperties;
	}

	public void prepare() {
		RandomManager.initialize(AvppsRandomSeed);
		if (source == null) { // then a default solution is used
			// 1. load pps from file
			String fileNameGas = "data/" + ExperimentParameterLiterals.getFileName(numberOfPlants, PowerplantType.GAS);
			String fileNameBio = "data/" + ExperimentParameterLiterals.getFileName(numberOfPlants, PowerplantType.BIO);

			source = new FilePowerPlantSource(fileNameGas, fileNameBio);
		}
		AvppCreator creator = populateAvppGraph();
		creator.printGraph(avppGraph);

		// 4. load residual load from file
		ResidualLoadReader residulalLoadReader = new ResidualLoadReader();
		residualLoad = residulalLoadReader.readLoad(ExperimentParameterLiterals.residualLoadFile);

		maxProduction = 0.0;
		for (PowerPlantData pd : allPlants) {
			maxProduction += pd.getPowerBoundaries().max;
		}

		// norming residual load
		maxLoad = Double.NEGATIVE_INFINITY;
		minLoad = Double.POSITIVE_INFINITY;
		for (int i = 0; i < residualLoad.length; ++i) {
			if (residualLoad[i] < minLoad)
				minLoad = residualLoad[i];
			if (residualLoad[i] > maxLoad)
				maxLoad = residualLoad[i];
		}

		if (maxProduction < minLoad) {
			// let max production be 10% greater than maxLoad
			double newMax = maxProduction / 1.1;
			double percMin = minLoad / maxLoad;
			double newMin = newMax * percMin;
			double rangeOrig = maxLoad - minLoad, rangeNew = newMax - newMin;

			for (int i = 0; i < residualLoad.length; ++i) {
				residualLoad[i] = newMin + rangeNew * ((residualLoad[i] - minLoad) / rangeOrig);
			}
			minLoad = newMin;
			maxLoad = newMax;
		}

		// residual load length must be ge than experiment horizon
		if (residualLoad.length < experimentHorizon)
			throw new RuntimeException("HALT! Experiment horizon is longer than available consumption data");

		RandomManager.initialize(InitialStatesSeed);
		initialStates = new HashMap<String, PowerPlantState>();
		for (PowerPlantData pd : allPlants) {
			initialStates.put(pd.getName(), getRandomInitialState(pd));
		}

		// put some initial states also for AVPPs
		initializeStates(avppGraph);
	}

	private AvppCreator populateAvppGraph() {
		allPlants = source.drawPowerPlants();

		RandomManager.initialize(HierarchyRandomSeed);

		AvppCreator ac = new AvppCreator();
		if (hierarchyType == HierarchyType.FLAT) {
			avppGraph = ac.createFlatGraph(allPlants);
		} else {
			ac.setPlantsPerAvpp(plantsPerAvpp);
			ac.setAvppsPerAvpp(avppsPerAvpp);
			ac.setHierarchyType(hierarchyType == HierarchyType.ISO_SPLIT);
			avppGraph = ac.createGraph(allPlants);
		}
		return ac;
	}

	protected void initializeStates(AvppGraph node) {
		if (node instanceof AvppLeafNode)
			return;

		double production = 0.0;
		for (AvppGraph child : node.getChildren()) {
			initializeStates(child);
			PowerPlantState childState = initialStates.get(child.getPowerPlant().getName());
			production += childState.getPower().min;
		}

		PowerPlantState avppState = new PowerPlantState();
		avppState.setPower(new Interval<Double>(production));
		boolean isRunning = production > 0;
		avppState.setRunning(new Interval<Boolean>(production > 0));
		avppState.setConsRunning(new Interval<Integer>(isRunning ? 1 : 0));
		avppState.setConsStopping(new Interval<Integer>(isRunning ? 0 : 1));
		avppState.setData(node.getPowerPlant());
		initialStates.put(node.getPowerPlant().getName(), avppState);
	}

	/**
	 * Takes values from state nodes and reinserts them into abstraction parameters at each time step
	 * 
	 * @param node
	 */
	protected void updateStates(AvppGraph node) {
		if (node instanceof AvppLeafNode)
			return;

		double avppProduction = 0.0;
		for (AvppGraph child : node.getChildren()) {
			updateStates(child);
			PowerPlantState childState = allStates.get(child.getPowerPlant().getName());
			avppProduction += childState.getPower().min;
			PowerPlantData pdata = childState.getData();

			if (!pdata.isAVPP()) {
				pdata.put(AbstractionParameterLiterals.CONSRUNNING_INIT, childState.getConsRunning().min.toString());
				pdata.put(AbstractionParameterLiterals.CONSSTOPPING_INIT, childState.getConsStopping().min.toString());
				pdata.put(AbstractionParameterLiterals.POWER_INIT, Double.toString(childState.getPower().min));
			}
		}

		PowerPlantState avppState = allStates.get(node.getPowerPlant().getName());

		// make sure avppProduction is actually bounded by min/max
		// TODO this might be too coarse, we may have to deal with all feasible regions
		avppProduction = CplexTools.convertCplexFloatIntoJavaDouble(node.getPowerPlant().getFeasibleRegions().first().min, node.getPowerPlant()
				.getFeasibleRegions().last().max, avppProduction);
		avppState.setPower(new Interval<Double>(avppProduction));
		avppState.setRunning(new Interval<Boolean>(avppProduction > 0));
	}

	protected HashMap<String, PowerPlantState> getDeepStateCopy(Map<String, PowerPlantState> initialStates2) {
		HashMap<String, PowerPlantState> deepCopy = new HashMap<String, PowerPlantState>(initialStates2);
		for (Entry<String, PowerPlantState> entry : initialStates2.entrySet()) {
			PowerPlantState state = entry.getValue().copy();
			deepCopy.put(entry.getKey(), state);
		}
		return deepCopy;
	}

	protected PowerPlantState getRandomInitialState(PowerPlantData pd) {
		PowerPlantState ps = new PowerPlantState();
		if ((!disconnectable) || RandomManager.getBoolean(.5)) {
			ps.setRunning(new Interval<Boolean>(true, true));
			ps.setConsRunning(new Interval<Integer>(1));
			ps.setConsStopping(new Interval<Integer>(0));
			double initPower = RandomManager.getDouble(pd.getPowerBoundaries().min, pd.getPowerBoundaries().max);
			ps.setPower(new Interval<Double>(initPower));
		} else {
			ps.setRunning(new Interval<Boolean>(false));
			ps.setConsRunning(new Interval<Integer>(0));
			ps.setConsStopping(new Interval<Integer>(1));
			ps.setPower(new Interval<Double>(0.0));
		}
		ps.setData(pd);
		return ps;
	}

	public void run() {
		experimentStartTime = new Date().getTime();
		timer = new Timer();
		statistics.addTimeStamp(experimentStartTime);

		prepare();
		String s = preanalyze();
		System.out.println(s);
		long elapsed = 0;
		if (solveHierarchically) {
			reset();
			timer.tick(TimerCategory.TOTAL_RUNTIME_REGIOCENTRAL.id);
			runRegioCentral();
			elapsed = timer.tock(TimerCategory.TOTAL_RUNTIME_REGIOCENTRAL.id);
			statistics.addTotalRuntimeRegioCentral(elapsed);
			reportRegioCentral();
		}

		if (solveCentrally) {
			reset();
			timer.tick(TimerCategory.TOTAL_RUNTIME_CENTRAL.id);
			runCentralized();
			elapsed = timer.tock(TimerCategory.TOTAL_RUNTIME_CENTRAL.id);
			statistics.addTotalRuntimeCentral(elapsed);
			reportCentralized();
		}

		String report = s + "\n" + statistics.writeStatistics() + "\n" + writeProperties(originatingProperties);
		System.out.println(report);

		java.io.File statsFile = new java.io.File("results/stats" + (experimentStartTime));
		Utils.writeFile(statsFile.getAbsolutePath(), report);
		statistics.reset(); // prepare for another run() call
		System.gc();
	}

	protected String writeProperties(Properties originatingProperties2) {
		StringBuilder sb = new StringBuilder();
		for (Object key : originatingProperties2.keySet()) {
			String strKey = (String) key;
			sb.append(strKey + "=" + originatingProperties2.getProperty(strKey) + "\n");
		}
		return sb.toString();
	}

	protected String preanalyze() {

		StringBuilder sb = new StringBuilder();
		sb.append("-----------------------\n");
		sb.append("Minimal residual load: " + minLoad + "\n");
		sb.append("Maximal residual load: " + maxLoad + "\n");
		sb.append("Maximal production: " + maxProduction + "\n");
		sb.append("Number of plants: " + allPlants.size() + "\n");
		sb.append("AVPP graph depth: " + avppGraph.getHeight() + "\n");
		sb.append("-----------------------\n");
		return sb.toString();
	}

	protected void reportRegioCentral() {
		String fileName = "results/regio-central-" + experimentStartTime + ".csv";
		Utils.writeFile(fileName, violationBookmarkBuilder.toString());
	}

	protected void reportCentralized() {
		String fileName = "results/central-" + experimentStartTime + ".csv";
		Utils.writeFile(fileName, violationBookmarkBuilder.toString());
	}

	protected void reset() {
		allStates = getDeepStateCopy(initialStates);
		violationBookmarkBuilder = new StringBuilder();
	}

	/**
	 * Recursively performs abstraction of constraint models
	 * 
	 * @param node
	 */
	protected void performAbstraction(AvppGraph node) {
		if (node instanceof AvppLeafNode) // nothing to do
			return;

		// postfix traversal - first abstract children
		List<PowerPlantData> childPlants = new ArrayList<PowerPlantData>(node.getChildren().size());
		for (AvppGraph child : node.getChildren()) {
			performAbstraction(child);
			childPlants.add(child.getPowerPlant());
		}

		System.out.println("Starting abstraction for " + node.getPowerPlant().getName());
		timer.tick(TimerCategory.ABSTRACT_AVPP_RUNTIME.id);

		// now children have all their feasible regions etc -> do that for me as
		// well
		GeneralAbstraction ga = new GeneralAbstraction();
		node.setGeneralAbstraction(ga);
		ga.setPowerPlants(childPlants);
		ga.perform();
		PowerPlantData nodeData = node.getPowerPlant();
		nodeData.setFeasibleRegions(ga.getFeasibleRegions());
		nodeData.setHoles(ga.getHoles());

		if (useSamplingAbstraction) {
			SamplingAbstraction sa = new SamplingAbstraction(ga.getFeasibleRegions(), ga.getHoles());
			sa.setInitialSamplingPoints(initialSamplingPoints);

			// get concrete cplex solver
			AbstractSolver solver = solverFactory.createSolver();
			AbstractModel model = solverFactory.createModel();
			model.setCostsInCents(useCostsInCents);
			model.setCosts(true);
			model.setPlantData(node.getPowerPlant(), node.getChildrenPlantData());
			Collection<String> dexprs = new ArrayList<String>(2);

			String costsObjective = AbstractionParameterLiterals.DEXP_COSTS + "[0]";
			if (useCostsInCents)
				costsObjective = "(" + costsObjective + ")/100.0";
			dexprs.add(AbstractionParameterLiterals.DEXP_COSTS + "Init = " + costsObjective);
			dexprs.add(AbstractionParameterLiterals.DEXP_POWER + "Init = " + AbstractionParameterLiterals.DEXP_POWER + "[0]");
			dexprs.add(AbstractionParameterLiterals.DEXP_POWER + "Succ = " + AbstractionParameterLiterals.DEXP_POWER + "[1]");
			model.addDecisionExpressions(dexprs);
			solver.setModel(model);

			sa.setMaximizationDecisionExpressions(Arrays.asList(AbstractionParameterLiterals.DEXP_POWER + "Succ"));
			sa.setMinimizationDecisionExpressions(Arrays.asList(AbstractionParameterLiterals.DEXP_COSTS + "Init", AbstractionParameterLiterals.DEXP_POWER
					+ "Succ"));
			sa.setSolver(solver);

			sa.setUseStaticSampling(useStaticSampling);
			sa.perform(samplingPoints);

			// reintegrate sampling points
			PiecewiseLinearFunction costFunction = sa.getPiecewiseLinearFunction(AbstractionParameterLiterals.DEXP_COSTS + "Init", true);
			node.getPowerPlant().setPositiveDelta(sa.getPiecewiseLinearFunction(AbstractionParameterLiterals.DEXP_POWER + "Succ", false));
			node.getPowerPlant().setCostFunction(costFunction);
			node.getPowerPlant().setNegativeDelta(sa.getPiecewiseLinearFunction(AbstractionParameterLiterals.DEXP_POWER + "Succ", true));
		}
		long elapsed = timer.tock(TimerCategory.ABSTRACT_AVPP_RUNTIME.id);
		statistics.addAvppAbstractionRuntime(elapsed);
	}

	protected void performTemporalAbstraction(AvppGraph node) {
		if (node instanceof AvppLeafNode) // nothing to do
			return;

		List<PowerPlantData> childPlants = new ArrayList<PowerPlantData>(node.getChildren().size());
		for (AvppGraph child : node.getChildren()) {
			performTemporalAbstraction(child);
			childPlants.add(child.getPowerPlant());
		}

		TemporalAbstraction ta = new TemporalAbstraction();
		node.setTemporalAbstraction(ta);
		ta.setPowerPlants(childPlants);
		ta.setGeneralHoles(node.getGeneralAbstraction().getHoles());
		ta.setGeneralFeasibleRegions(node.getGeneralAbstraction().getFeasibleRegions());

		ta.perform(timeHorizon);
		PowerPlantData pd = node.getPowerPlant();
		pd.setAllFeasibleRegions(ta.getAllFeasibleRegions());
		pd.setAllHoles(ta.getAllHoles());
	}

	protected void runRegioCentral() {
		System.out.println("========================= REGIO CENTRAL ===================");
		CplexExporter exporter = new CplexExporter();
		CplexAvppGraphExporter graphExporter = new CplexAvppGraphExporter(exporter);
		exporter.setTimeHorizon(getTimeHorizon());
		exporter.setResidualLoad(residualLoad);
		exporter.setUseSamplingAbstraction(true);
		exporter.setUseTemporalAbstraction(useTemporalAbstraction);
		exporter.setUseGeneralFeasibleRegions(true);
		exporter.setUseCompleteRange(false); // starts at 1
		exporter.setCosts(true);
		exporter.setUseCostsInCents(useCostsInCents);
		exporter.setOptimizationFunction("overallCosts", true);
		// exporter.setOptimizationFunction(decExpr, minimize);
		graphExporter.createRegionalModels(avppGraph);

		extractConcretePlants();
		// perform general abstraction and sampling abstraction bottom up
		timer.tick(TimerCategory.ABSTRACTION_RUNTIME.id);
		performAbstraction(avppGraph);
		long elapsed = timer.tock(TimerCategory.ABSTRACTION_RUNTIME.id);

		solverFacade.setTimeLimit(60);
		solverFacade.setUseInitialSolution(false);
		statistics.addAbstractionRuntime(elapsed);
		statistics.setFixedAbstractionRuntime(elapsed);
		// solve model by creating decentralized models using avpps
		for (int t = 0; t < experimentHorizon - timeHorizon; ++t) {
			// update states from concrete power plants
			updateStates(avppGraph);
			resetStatsMaps();

			timer.tick(TimerCategory.RUNTIME_REGIOCENTRAL_TS.id);
			// perform temporal abstraction with current state
			timer.tick(TimerCategory.ABSTRACTION_RUNTIME.id);
			if (useTemporalAbstraction)
				performTemporalAbstraction(avppGraph);

			elapsed = timer.tock(TimerCategory.ABSTRACTION_RUNTIME.id);
			statistics.addAbstractionRuntime(statistics.getAbstractionRuntime() + elapsed);
			statistics.addVariableAbstractionTime(elapsed);

			Double[] residualLoadPiece = getResidualLoad(residualLoad, t, timeHorizon);

			currentLongestSerialPath = 0; // reset the current lsp value
			// calls recursive solving algorithm
			solveRecursively(avppGraph, graphExporter, t, residualLoadPiece, 0);
			statistics.reportSerialPath(currentLongestSerialPath);

			long elapsedStep = timer.tock(TimerCategory.RUNTIME_REGIOCENTRAL_TS.id);
			statistics.addRegioCentralRuntimePerStep(elapsedStep);
			// manage abstraction error
			statistics.reportAbstractionError(avppGraph.getPowerPlant().getName(), loadInputs, actualLoads);

			// get aggregated production and report it
			double totalProduction = 0.0;
			double totalCosts = 0.0;
			for (PowerPlantState state : concretePlantStates.values()) {
				double productionNow = state.getPower().min;
				totalProduction += productionNow;
				PowerPlantData plant = state.getData();
				double nextCosts = plant.getCostFunction().evaluate(productionNow);
				totalCosts += nextCosts;
			}

			// also find out cost of top level avpp itself to compare abstracted (estimated) and actual costs
			double abstractTotalCosts = 0.0;
			for (PowerPlantData rootChild : avppGraph.getChildrenPlantData()) {
				// just first level direct children
				PowerPlantState state = allStates.get(rootChild.getName());
				double contribution = state.getPower().min;
				double costAbstract = rootChild.getCostFunction().evaluate(contribution);
				abstractTotalCosts += costAbstract;
			}

			bookmark(t, residualLoad[t], totalProduction);
			statistics.addToplevelViolationRegioCentral(totalProduction, residualLoad[t]);
			statistics.addToplevelCostsRegioCentral(totalCosts);
			statistics.addRootCostsRegioCentral(abstractTotalCosts);
		}
		// compare overall performance
	}

	protected void resetStatsMaps() {
		loadInputs = new HashMap<String, Double>();
		actualLoads = new HashMap<String, Double>();
	}

	protected void solveRecursively(AvppGraph node, CplexAvppGraphExporter graphExporter, int t, Double[] residualLoadPerNode, long elapsedUntil) {
		// first solve, then call recursively for children
		timer.tick(TimerCategory.AVPP_TIME.id);
		loadInputs.put(node.getPowerPlant().getName(), residualLoadPerNode[0]);

		Map<String, PowerPlantState> localStates = getStates(node, allStates);

		// print top level model
		String modelFile = "generated/" + node.getPowerPlant().getName() + ".mod";
		Utils.writeFile(modelFile, node.getCplexModel());
		String generalAbstractionData = graphExporter.getGeneralAbstractionData(node);
		String temporalAbstractionData = "";
		if (useTemporalAbstraction)
			temporalAbstractionData = graphExporter.getTemporalAbstractionData(node);

		String piecewiseData = "";
		if (useSamplingAbstraction) {
			piecewiseData = graphExporter.getExporter().writePiecewiseLinearData(node.getChildrenPlantData());
		}

		String generalSelfAbstraction = graphExporter.getExporter().getTotalProductionAbstractionString(node.getGeneralAbstraction().getHoles(),
				node.getGeneralAbstraction().getFeasibleRegions());
		String residualLoadStr = graphExporter.getExporter().createResidualLoad(residualLoadPerNode);

		String costStr = graphExporter.getExporter().writeCostsString(node.getChildrenPlantData());

		String initState = graphExporter.getExporter().createInitStateData(localStates);

		String dataFile = "generated/" + node.getPowerPlant().getName() + "_" + t + ".dat";
		String dataContent = generalAbstractionData + "\n" + temporalAbstractionData + "\n" + residualLoadStr + "\n" + piecewiseData + "\n" + initState + "\n"
				+ costStr + "\n" + generalSelfAbstraction;
		Utils.writeFile(dataFile, dataContent);

		solverFacade.solve(modelFile, dataFile);
		// extract values for t=1 to be the next init
		if (!solverFacade.isSolved()) {
			// try simplified
			solverFacade.setSimplified(true);
			solverFacade.cleanup();

			solverFacade.solve(modelFile, dataFile);
			solverFacade.setSimplified(false);

			if (!solverFacade.isSolved()) {
				solverFacade.setSimplified(true);
				solverFacade.setBackupSettings(true);
				solverFacade.cleanup();
				solverFacade.solve(modelFile, dataFile);
				solverFacade.setSimplified(false);
				solverFacade.setBackupSettings(false);
				if (!solverFacade.isSolved()) {
					solverFacade.cleanup();
					throw new RuntimeException("Model " + modelFile + " / " + dataFile + " could not be solved!");
				}
			}

		}
		for (Entry<String, PowerPlantState> state : localStates.entrySet()) {
			double power = solverFacade.getProduction(state.getKey(), 1);
			if (power < jitter)
				power = 0;
			boolean running = solverFacade.getRunning(state.getKey(), 1);

			state.getValue().setPower(new Interval<Double>(power));
			state.getValue().setRunning(new Interval<Boolean>(running));
		}

		// get total production for comparison with input
		double totalPower = solverFacade.getTotalProduction(1);
		actualLoads.put(node.getPowerPlant().getName(), totalPower);
		// solve children
		HashMap<String, Double[]> childLoads = new HashMap<String, Double[]>(node.getChildren().size() * 2);

		// first iterate to get residual loads, then call solver again
		for (AvppGraph childNode : node.getChildren()) {
			if (!(childNode instanceof AvppLeafNode)) {
				// extract powers
				Double[] remainingLoads = new Double[timeHorizon];
				for (int t_ = 0; t_ < timeHorizon; ++t_) {
					remainingLoads[t_] = solverFacade.getProduction(childNode.getPowerPlant().getName(), t_ + 1);
				}
				childLoads.put(childNode.getPowerPlant().getName(), remainingLoads);

			}
		}

		long elapsed = timer.tock(TimerCategory.AVPP_TIME.id);

		statistics.addAvppRuntime(elapsed);
		// solve for child TODO maybe with new thread here
		// cleanup solver here
		solverFacade.cleanup();

		// delete model and dat file
		Utils.deleteFile(modelFile);
		Utils.deleteFile(dataFile);
		for (AvppGraph childNode : node.getChildren()) {
			if (!(childNode instanceof AvppLeafNode)) {
				solveRecursively(childNode, graphExporter, t, childLoads.get(childNode.getPowerPlant().getName()), elapsedUntil + elapsed);
			}
		}

		// report serial path
		reportSerialPathLocally(elapsed + elapsedUntil);
	}

	private void reportSerialPathLocally(long longestCandidate) {
		currentLongestSerialPath = Math.max(currentLongestSerialPath, longestCandidate);
	}

	protected Map<String, PowerPlantState> getStates(AvppGraph node, Map<String, PowerPlantState> allStates2) {
		Map<String, PowerPlantState> localStates = new HashMap<String, PowerPlantState>(node.getChildren().size() * 2);

		for (AvppGraph child : node.getChildren()) {
			localStates.put(child.getPowerPlant().getName(), allStates2.get(child.getPowerPlant().getName()));
		}
		return localStates;
	}

	protected Double[] getResidualLoad(Double[] residualLoadParam, int t, int timeHorizon) {
		Double[] residualLoad2 = new Double[timeHorizon];
		for (int inc = 0; inc < timeHorizon; ++inc) {
			residualLoad2[inc] = residualLoadParam[t + inc];
		}
		return residualLoad2;
	}

	protected void runCentralized() {
		CplexExporter exporter = new CplexExporter();
		exporter.setTimeHorizon(getTimeHorizon());
		exporter.setResidualLoad(residualLoad);
		exporter.setMaximalUpperBound(maxProduction);
		exporter.setUseCompleteRange(false); // starts at 1
		exporter.setCosts(true);
		exporter.setUseCostsInCents(useCostsInCents);
		exporter.setOptimizationFunction("overallCosts", true);

		CplexAvppGraphExporter graphExporter = new CplexAvppGraphExporter(exporter);
		extractConcretePlants();
		String s = graphExporter.createSingleModel(avppGraph);

		String modelFile = "generated/file001.mod";
		String modelFileFeatures = null;

		Utils.writeFile(modelFile, s);
		if (useFeaturesAlgorithm) {
			Date stampDate = new Date();
			modelFileFeatures = "generated/Evaluation/model_" + stampDate.getTime() + ".mod";
			Utils.writeFile(modelFileFeatures, s);
		}
		solverFacade.setTimeLimit(1800); // 30 min initial time limit
		solverFacade.setPresolve(true);
		solverFacade.setUseInitialSolution(false);
		// main loop
		for (int t = 0; t < experimentHorizon - timeHorizon; ++t) {
			timer.tick(TimerCategory.RUNTIME_CENTRAL_TS.id);
			// create new .dat file with the current states
			String initState = exporter.createInitStateData(concretePlantStates);
			Double[] residualLoadPiece = getResidualLoad(residualLoad, t, timeHorizon);
			String residualLoadStr = "\n" + exporter.createResidualLoad(residualLoadPiece);
			String costStr = "\n" + exporter.writeCostsString(allPlants);
			String dataFile = "generated/central_state" + t + ".mod";

			String dataContent = initState + residualLoadStr + costStr;
			Utils.writeFile(dataFile, dataContent);

			String dataFileFeatures = null;
			int noRunningPrior = 0;
			double avgRelLoadPrior = 0.0;
			if (useFeaturesAlgorithm) {
				Date stampDate = new Date();
				dataFileFeatures = "generated/Evaluation/data_" + (stampDate.getTime() + t) + ".dat";
				Utils.writeFile(dataFileFeatures, dataContent);

				// analyze current states
				double sumRel = 0.0;
				for (PowerPlantState state : concretePlantStates.values()) {
					PowerPlantData data = state.getData();
					boolean priorRunning = state.isRunning().min;
					if (priorRunning)
						++noRunningPrior;

					double priorPower = state.getPower().min;
					// maybe relative to P max
					double pMax = data.getFeasibleRegions().last().max;

					double rel = priorPower / pMax;
					sumRel += rel;
				}
				avgRelLoadPrior = sumRel / concretePlantStates.values().size();
			}

			// run model with new .dat file
			solverFacade.solve(modelFile, dataFile);

			// extract values for t=1 to be the next init
			int noRunningPosterior = 0;
			double avgRelLoadPosterior = 0.0;

			if (solverFacade.isSolved()) {
				double sumRel = 0.0;
				for (Entry<String, PowerPlantState> state : concretePlantStates.entrySet()) {
					double power = solverFacade.getProduction(state.getKey(), 1);
					boolean running = solverFacade.getRunning(state.getKey(), 1);

					if (power < jitter)
						power = 0;
					state.getValue().setPower(new Interval<Double>(power));
					state.getValue().setRunning(new Interval<Boolean>(running));

					if (useFeaturesAlgorithm) {
						if (running)
							++noRunningPosterior;
						double pMax = state.getValue().getData().getFeasibleRegions().last().max;

						double rel = power / pMax;
						sumRel += rel;
					}
				}
				if (useFeaturesAlgorithm)
					avgRelLoadPosterior = sumRel / concretePlantStates.entrySet().size();
				statistics.reportUnsolvedAllocation(0.0);
			} // else everything stays the same
			else {
				statistics.reportUnsolvedAllocation(1.0);
			}
			solverFacade.cleanup();

			// store quantities of interest (cost, violation ...) for evaluation
			double totalProduction = getTotalProduction(concretePlantStates);
			double totalCosts = getTotalCosts(concretePlantStates);
			long elapsed = timer.tock(TimerCategory.RUNTIME_CENTRAL_TS.id);
			statistics.addCentralRuntimePerStep(elapsed);
			bookmark(t, residualLoad[t], totalProduction);
			statistics.addToplevelViolationCentral(totalProduction, residualLoad[t]);
			statistics.addToplevelCostsCentral(totalCosts);

			// now for the exploratory data analysis part
			if (useFeaturesAlgorithm) {
				features.addFeature("CountPlants", Integer.toString(allPlants.size()));
				features.addFeature("Runtime", Double.toString(ExperimentStatistics.toSeconds(elapsed)));

				for (int j = 0; j < residualLoadPiece.length; ++j) {
					features.addFeature("Demand-" + (j + 1), Double.toString(residualLoadPiece[j]));
				}

				features.addFeature("ModelFile", modelFileFeatures);
				features.addFeature("DataFile", dataFileFeatures);

				features.addFeature("AvgLoadPrior", Double.toString(avgRelLoadPrior));
				features.addFeature("AvgLoadPosterior", Double.toString(avgRelLoadPosterior));

				features.addFeature("NoRunningPrior", Integer.toString(noRunningPrior));
				features.addFeature("NoRunningPosterior", Integer.toString(noRunningPosterior));
			}
		}
	}

	protected void extractConcretePlants() {
		concretePlantStates = new HashMap<String, PowerPlantState>(allStates.size());
		for (Entry<String, PowerPlantState> state : allStates.entrySet())
			if (!state.getValue().getData().isAVPP())
				concretePlantStates.put(state.getKey(), state.getValue());

	}

	protected void bookmark(int t, Double residualLoad, double totalProduction) {
		violationBookmarkBuilder.append(t + ";" + residualLoad + ";" + totalProduction + "\n");
	}

	protected double getTotalProduction(Map<String, PowerPlantState> concretePlantStates2) {
		double production = 0.0;
		for (PowerPlantState state : concretePlantStates2.values()) {
			production += state.getPower().min;
		}
		return production;
	}

	/**
	 * Inspects how much a power plant produced given a state and uses its cost function to get summed prices
	 * 
	 * @param powerPlantStates
	 * @return
	 */
	protected double getTotalCosts(Map<String, PowerPlantState> powerPlantStates) {
		double totalCosts = 0.0;
		for (PowerPlantState state : powerPlantStates.values()) {
			double individualProduction = state.getPower().min;
			PowerPlantData data = state.getData();
			double individualCosts = data.getCostFunction().evaluate(individualProduction);
			totalCosts += individualCosts;
		}
		return totalCosts;
	}

	public int getTimeHorizon() {
		return timeHorizon;
	}

	public void setTimeHorizon(int timeHorizon) {
		this.timeHorizon = timeHorizon;
	}

	public ExperimentParameterLiterals.NumberPlants getNumberOfPlants() {
		return numberOfPlants;
	}

	public void setNumberOfPlants(ExperimentParameterLiterals.NumberPlants numberOfPlants) {
		this.numberOfPlants = numberOfPlants;
	}

	public int getPlantsPerAvpp() {
		return plantsPerAvpp;
	}

	public void setPlantsPerAvpp(int plantsPerAvpp) {
		this.plantsPerAvpp = plantsPerAvpp;
	}

	public int getExperimentHorizon() {
		return experimentHorizon;
	}

	public void setExperimentHorizon(int experimentHorizon) {
		this.experimentHorizon = experimentHorizon;
	}

	public SolverFacade getSolverFacade() {
		return solverFacade;
	}

	public void setSolverFacade(SolverFacade solver) {
		this.solverFacade = solver;
	}

	public AbstractSolverFactory getSolverFactory() {
		return solverFactory;
	}

	public void setSolverFactory(AbstractSolverFactory solverFactory) {
		this.solverFactory = solverFactory;
	}

	public boolean isUseSamplingAbstraction() {
		return useSamplingAbstraction;
	}

	public void setUseSamplingAbstraction(boolean useSamplingAbstraction) {
		this.useSamplingAbstraction = useSamplingAbstraction;
	}

	public int getSamplingPoints() {
		return samplingPoints;
	}

	public void setSamplingPoints(int samplingPoints) {
		this.samplingPoints = samplingPoints;
	}

	public void setHierarchyType(HierarchyType hierarchyType) {
		this.hierarchyType = hierarchyType;
	}

	public long getHierarchyRandomSeed() {
		return HierarchyRandomSeed;
	}

	public void setHierarchyRandomSeed(long hierarchyRandomSeed) {
		HierarchyRandomSeed = hierarchyRandomSeed;
	}

	public long getAvppsRandomSeed() {
		return AvppsRandomSeed;
	}

	public void setAvppsRandomSeed(long avppsRandomSeed) {
		AvppsRandomSeed = avppsRandomSeed;
	}

	public long getInitialStatesSeed() {
		return InitialStatesSeed;
	}

	public void setInitialStatesSeed(long initialStatesSeed) {
		InitialStatesSeed = initialStatesSeed;
	}

	public int getAvppsPerAvpp() {
		return avppsPerAvpp;
	}

	public void setAvppsPerAvpp(int avppsPerAvpp) {
		this.avppsPerAvpp = avppsPerAvpp;
	}

	public PowerPlantSource getSource() {
		return source;
	}

	public void setSource(PowerPlantSource source) {
		this.source = source;
	}

	public int getCountPlants() {
		return countPlants;
	}

	public void setCountPlants(int countPlants) {
		this.countPlants = countPlants;
	}

	public void setDisconnectable(int readProperty) {
		this.disconnectable = (readProperty == 1);
	}

	public boolean isDisconnectable() {
		return disconnectable;
	}

	public ExperimentStatistics getStatistics() {
		return statistics;
	}

	public void setStatistics(ExperimentStatistics statistics) {
		this.statistics = statistics;
	}

	public boolean isSolveCentrally() {
		return solveCentrally;
	}

	public void setSolveCentrally(boolean solveCentrally) {
		this.solveCentrally = solveCentrally;
	}

	public boolean isUseStaticSampling() {
		return useStaticSampling;
	}

	public void setUseStaticSampling(boolean useStaticSampling) {
		this.useStaticSampling = useStaticSampling;
	}

	public boolean isSolveHierarchically() {
		return solveHierarchically;
	}

	public void setSolveHierarchically(boolean solveHierarchically) {
		this.solveHierarchically = solveHierarchically;
	}

	public boolean isUseTemporalAbstraction() {
		return useTemporalAbstraction;
	}

	public void setUseTemporalAbstraction(boolean useTemporalAbstraction) {
		this.useTemporalAbstraction = useTemporalAbstraction;
	}

	public int getMinutesPerTimestep() {
		return minutesPerTimestep;
	}

	public void setMinutesPerTimestep(int minutesPerTimestep) {
		this.minutesPerTimestep = minutesPerTimestep;
	}

	public boolean isDetailedInertia() {
		return detailedInertia;
	}

	public void setDetailedInertia(boolean detailedInertia) {
		this.detailedInertia = detailedInertia;
	}

	public boolean isUseCostsInCents() {
		return useCostsInCents;
	}

	public void setUseCostsInCents(boolean useCostsInCents) {
		this.useCostsInCents = useCostsInCents;
	}

	public void setFeaturesAlgorithm(boolean featuresAlgorithm) {
		this.useFeaturesAlgorithm = featuresAlgorithm;
	}

	public boolean isUseFeaturesAlgorithm() {
		return useFeaturesAlgorithm;
	}

	public void setUseFeaturesAlgorithm(boolean useFeaturesAlgorithm) {
		this.useFeaturesAlgorithm = useFeaturesAlgorithm;
	}

	public AlgorithmFeatures getFeatures() {
		return features;
	}

	public void setFeatures(AlgorithmFeatures features) {
		this.features = features;
	}

	public void setInitialSamplingPoints(int initialSamplingPoints) {
		// these are initially available for the active learner
		this.initialSamplingPoints = initialSamplingPoints;
	}

	public int getInitialSamplingPoints() {
		return initialSamplingPoints;
	}

}

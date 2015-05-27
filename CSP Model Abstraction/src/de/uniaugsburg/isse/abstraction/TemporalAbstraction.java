package de.uniaugsburg.isse.abstraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

import de.uniaugsburg.isse.abstraction.merging.HoleDetection;
import de.uniaugsburg.isse.abstraction.types.Interval;
import de.uniaugsburg.isse.abstraction.types.IntervalSet;
import de.uniaugsburg.isse.abstraction.types.PiecewiseLinearFunction;
import de.uniaugsburg.isse.constraints.Constraint;
import de.uniaugsburg.isse.powerplants.PowerPlantData;
import de.uniaugsburg.isse.powerplants.PowerPlantState;
import de.uniaugsburg.isse.util.PowerPlantUtil;

/**
 * Performs temporally sensitive abstraction i.e. looks some timesteps ahead and returns boundaries and holes for some
 * future
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class TemporalAbstraction extends GeneralAbstraction {

	protected Collection<Constraint> temporalConstraint;
	protected List<SortedSet<Interval<Double>>> allFeasibleRegions;
	protected List<Collection<Interval<Double>>> allHoles;
	private final Double delta = 2.0; // delta to avoid having single points when

	// TODO I know this should come from TEG but is set statically for now
	private double deltaTime = 15.0;

	// starting up

	public double getDeltaTime() {
		return deltaTime;
	}

	public void setDeltaTime(double deltaTime) {
		this.deltaTime = deltaTime;
	}

	/**
	 * Internally performs the temporal abstraction
	 * 
	 * @param T
	 * @param plantStates
	 * @param avpps
	 */
	protected IntervalSet perform(int T, Collection<PowerPlantState> plantStates, Collection<PowerPlantData> avpps) {
		HoleDetection hd = new HoleDetection();

		ArrayList<SortedSet<Interval<Double>>> allFeasibleRegionsTemp = new ArrayList<SortedSet<Interval<Double>>>();
		ArrayList<Collection<Interval<Double>>> allHolesTemp = new ArrayList<Collection<Interval<Double>>>();

		SortedSet<Interval<Double>> feasibleRegions;
		Collection<Interval<Double>> holes;

		boolean reachHorizon = false; // all possible states can be reached
		for (int t = 1; t <= T && !reachHorizon; ++t) {
			// System.out.println("* --------------------- t = " + t +
			// " ------------------- ");
			Collection<Collection<Interval<Double>>> plantIntervalsList = new ArrayList<Collection<Interval<Double>>>();

			for (PowerPlantState pp : plantStates) {
				pp.setSimulationStep(t);
				// start with initial values for P_(t+1)
				double P_min_t_inc = Double.NEGATIVE_INFINITY;
				double P_max_t_inc = Double.POSITIVE_INFINITY;

				// same for On_(t+1)
				boolean On_min_t_inc = false;
				boolean On_max_t_inc = true;
				// System.out.println("Looking at: " + pp.getName());

				for (Constraint c : pp.getData().getAssociatedConstraints()) {
					c.setDeltaTime(deltaTime);
					if (!c.isSoft()) {
						// minimize step
						P_min_t_inc = Math.max(c.minimize(), P_min_t_inc);
						// none may say that pp can be on - default is false!
						On_min_t_inc = c.minimizeBool() || On_min_t_inc;

						// maximize step
						P_max_t_inc = Math.min(c.maximize(), P_max_t_inc);
						// all have to allow pp to be on - default is true
						On_max_t_inc = c.maximizeBool() && On_max_t_inc;
					}
				}

				pp.updateRunning(On_min_t_inc, On_max_t_inc);

				// System.out.println("------------ "+pp.getPower().min + " / "
				// + pp.getPower().max);
				if (!On_min_t_inc)
					P_min_t_inc = 0.0;
				if (On_max_t_inc) {
					P_max_t_inc = Math.max(P_max_t_inc, pp.getData().getPowerBoundaries().min);
				}
				pp.getPower().min = P_min_t_inc;
				pp.getPower().max = P_max_t_inc;

				// intervals to add
				double add_int_min = Math.max(P_min_t_inc, pp.getData().getPowerBoundaries().min);
				double add_int_max = Math.max(P_max_t_inc, pp.getData().getPowerBoundaries().min + this.delta);
				// System.out.println("------------ "+add_int_min + " / " +
				// add_int_max);

				Interval<Double> addInt = new Interval<Double>(add_int_min, add_int_max);
				Collection<Interval<Double>> setIntervals = new ArrayList<Interval<Double>>();
				if (pp.onOrOff() || pp.onlyOff()) {
					setIntervals.add(PowerPlantUtil.getZero());
				}
				if (!pp.onlyOff()) {
					setIntervals.add(addInt);
				}
				plantIntervalsList.add(setIntervals);

				// System.out.println(pp.printState());
			}

			for (PowerPlantData avpp : avpps) {
				List<SortedSet<Interval<Double>>> avppRegions = avpp.getAllFeasibleRegions();
				int index = t - 1;

				if (avppRegions.size() > index) {
					Collection<Interval<Double>> timeRegions = avppRegions.get(index);
					if (timeRegions.isEmpty()) {
						plantIntervalsList.add(avpp.getFeasibleRegions());
					} else {
						plantIntervalsList.add(timeRegions);
					}
				} else { // converged, use general abstraction
					plantIntervalsList.add(avpp.getFeasibleRegions());
				}
			}

			holes = hd.detectSupplyHolesNew(plantIntervalsList);
			feasibleRegions = hd.getIntervalList();

			reachHorizon = PowerPlantUtil.checkConvergence(feasibleRegions, this.getFeasibleRegions());
			// if the feasible regions turn out to be empty (i.e. only intermittent plants) -> do not add them and
			// converge (so general abstraction gets used anyway)
			if (!feasibleRegions.isEmpty()) {
				allFeasibleRegionsTemp.add(feasibleRegions);
				allHolesTemp.add(holes);
			} else
				reachHorizon = true;

		} // for t
		return new IntervalSet(allFeasibleRegionsTemp, allHolesTemp);
	}

	/**
	 * performs time sensitive abstraction up to some time step t maximizes and minimizes in each step to get boundaries
	 * of the AVPP
	 * 
	 * @param t
	 */
	public final void perform(int T) {
		// first order set by P_min

		ArrayList<PowerPlantState> plantStates = new ArrayList<PowerPlantState>(this.powerPlants.size());
		ArrayList<PowerPlantData> avpps = new ArrayList<PowerPlantData>(this.powerPlants.size());

		// collect initial state
		for (PowerPlantData pd : this.powerPlants) {
			if (pd.isAVPP()) {
				avpps.add(pd);
			} else {
				PowerPlantState newPlant = new PowerPlantState();
				newPlant.setData(pd);
				newPlant.initialize(); // take init values from data
				newPlant.updateConstraints();
				plantStates.add(newPlant);
			}
		}

		IntervalSet is = this.perform(T, plantStates, avpps);
		this.allFeasibleRegions = is.getAllFeasibleRegions();
		this.allHoles = is.getAllHoles();
	}

	public PiecewiseLinearFunction getMaxOutputFunctionByState(List<SortedSet<Interval<Double>>> allFeasibleRegions) {
		// find out max delta at minimum power

		Collection<Interval<Double>> inOutPairs = new LinkedList<Interval<Double>>();
		Collection<PowerPlantState> newPlantStates = new ArrayList<PowerPlantState>(this.powerPlants.size());
		Collection<PowerPlantData> avpps = new ArrayList<PowerPlantData>(0);
		double totalMin = 0.0;
		double totalNow = 0.0;

		for (PowerPlantData pd : this.powerPlants) {
			// find initial state and initialize
			PowerPlantState actualState = new PowerPlantState();
			actualState.setData(pd);
			actualState.initialize();

			totalNow += actualState.getPower().max;

			// initialize with min value and running 1
			PowerPlantState state = new PowerPlantState();
			state.setData(pd);
			state.setConsRunning(new Interval<Integer>(1));
			state.setConsStopping(new Interval<Integer>(0));
			double pMin = pd.getPowerBoundaries().min;
			totalMin += pMin;
			state.setPower(new Interval<Double>(pMin));
			state.updateConstraints();
			newPlantStates.add(state);
		}

		// perform TA with exactly one step, only if the AVPP is not extremal
		if (Math.abs(totalMin - totalNow) > 0.05) {
			IntervalSet iset = this.perform(1, newPlantStates, avpps);

			// find maximum from this step
			SortedSet<Interval<Double>> firstTimestepSet = iset.getAllFeasibleRegions().get(0);
			Double pMax = firstTimestepSet.last().max;

			inOutPairs.add(new Interval<Double>(totalMin, pMax));
		}

		// P_min -> P_min^
		// P_now -> P_next.max
		// P_next.max -> P_next.max.max etc ...

		// from the actual step onwards -> collect (P_now, P_next) pairs for a pw linear function
		double pNow = totalNow;

		for (SortedSet<Interval<Double>> currentTimeStepRegions : allFeasibleRegions) {
			double pNext = currentTimeStepRegions.last().max;

			inOutPairs.add(new Interval<Double>(pNow, pNext));
			if (Math.abs(pNow - pNext) < 0.001)
				break;

			pNow = pNext;
		}

		// if max. output can be reached from min. output in one time step
		if (inOutPairs.size() == 1) {
			double pMax = allFeasibleRegions.get(0).first().max;
			double in = pMax;
			double out = pMax + (pMax - totalNow);

			inOutPairs.add(new Interval<Double>(in, out));
		}

		PiecewiseLinearFunction positiveDelta = new PiecewiseLinearFunction();
		positiveDelta.convert(inOutPairs);
		return positiveDelta;
	}

	public PiecewiseLinearFunction getMinOutputFunctionByState(List<SortedSet<Interval<Double>>> allFeasibleRegions) {
		// find out max delta at minimum power

		LinkedList<Interval<Double>> inOutPairs = new LinkedList<Interval<Double>>();
		Collection<PowerPlantState> newPlantStates = new ArrayList<PowerPlantState>(this.powerPlants.size());
		Collection<PowerPlantData> avpps = new ArrayList<PowerPlantData>(0);
		double totalMax = 0.0;
		double totalNow = 0.0;

		for (PowerPlantData pd : this.powerPlants) {
			// find initial state and initialize
			PowerPlantState actualState = new PowerPlantState();
			actualState.setData(pd);
			actualState.initialize();

			totalNow += actualState.getPower().max;

			// initialize with min value and running 1
			PowerPlantState state = new PowerPlantState();
			state.setData(pd);
			state.setConsRunning(new Interval<Integer>(1));
			state.setConsStopping(new Interval<Integer>(0));
			double pMax = pd.getPowerBoundaries().max;
			totalMax += pMax;
			state.setPower(new Interval<Double>(pMax));
			state.updateConstraints();
			newPlantStates.add(state);
		}

		// perform TA with exactly one step only if the AVPP is not at an extremal value itself
		if (Math.abs(totalMax - totalNow) > 0.05) {
			IntervalSet iset = this.perform(1, newPlantStates, avpps);

			// find maximum from this step
			SortedSet<Interval<Double>> firstTimestepSet = iset.getAllFeasibleRegions().get(0);
			Double pMin = firstTimestepSet.first().min;

			inOutPairs.add(new Interval<Double>(totalMax, pMin));
		}

		// P_min -> P_min<
		// P_now -> P_next.min
		// P_next.min -> P_next.min.min etc ...

		// from the actual step onwards -> collect (P_now, P_next) pairs for a pw linear function
		double pNow = totalNow;

		for (SortedSet<Interval<Double>> currentTimeStepRegions : allFeasibleRegions) {
			double pNext = currentTimeStepRegions.first().min;

			inOutPairs.add(new Interval<Double>(pNow, pNext));
			if (Math.abs(pNow - pNext) < 0.001)
				break;

			pNow = pNext;
		}

		// if min. output can be reached from max. output in one time step
		if (inOutPairs.size() == 1) {
			double pMin = allFeasibleRegions.get(0).first().min;
			double in = pMin;
			double out = pMin - (totalMax - pMin);

			inOutPairs.add(new Interval<Double>(in, out));
		}

		PiecewiseLinearFunction negativeDelta = new PiecewiseLinearFunction();
		Collections.reverse(inOutPairs);
		negativeDelta.convert(inOutPairs);

		return negativeDelta;
	}

	public List<SortedSet<Interval<Double>>> getAllFeasibleRegions() {
		return this.allFeasibleRegions;
	}

	public void setAllFeasibleRegions(List<SortedSet<Interval<Double>>> allFeasibleRegions) {
		this.allFeasibleRegions = allFeasibleRegions;
	}

	public List<Collection<Interval<Double>>> getAllHoles() {
		return this.allHoles;
	}

	public void setAllHoles(List<Collection<Interval<Double>>> allHoles) {
		this.allHoles = allHoles;
	}

	public void printAll() {
		int t = 0;
		for (SortedSet<Interval<Double>> regions : this.allFeasibleRegions) {
			System.out.println("#### Feasible regions after t = " + (++t));
			for (Interval<Double> region : regions) {
				System.out.print(region + " , ");
			}
			System.out.println();
		}

	}
}

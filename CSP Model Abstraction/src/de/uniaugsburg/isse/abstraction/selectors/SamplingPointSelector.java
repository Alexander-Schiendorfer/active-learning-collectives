package de.uniaugsburg.isse.abstraction.selectors;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uniaugsburg.isse.abstraction.InOutPair;
import de.uniaugsburg.isse.abstraction.types.Interval;

/**
 * Represents a strategy to select the next input to ask for in sampling abstraction
 * 
 * @author alexander
 *
 */
public abstract class SamplingPointSelector {

	protected SortedSet<Interval<Double>> generalFeasibleRegions;
	protected Collection<InOutPair> sampledPoints;
	protected Collection<Interval<Double>> generalHoles;

	/**
	 * Defines the base set a sampling point selector may rely on
	 * 
	 * @param inputs
	 */
	public void setInitialPoints(Collection<InOutPair> initialPoints) {
		this.sampledPoints = new TreeSet<InOutPair>(initialPoints);
		consumeInitialPoints(sampledPoints);
	}

	/**
	 * Template method for sampled points
	 * 
	 * @param sampledPoints
	 */
	protected void consumeInitialPoints(Collection<InOutPair> sampledPoints) {
	}

	/**
	 * Prepares general abstraction data, i.e., feasible regions and holes
	 * 
	 * @param generalFeasibleRegions
	 * @param generalHoles
	 */
	public void setAbstractionData(SortedSet<Interval<Double>> generalFeasibleRegions, Collection<Interval<Double>> generalHoles) {
		this.generalFeasibleRegions = generalFeasibleRegions;
		this.generalHoles = generalHoles;
	}

	public abstract boolean hasNext();

	public abstract double getNextInput();

	public void inform(InOutPair pair) {
		sampledPoints.add(pair);
		consume(pair);
	}

	/**
	 * Template method for a single sampled in out pair
	 * 
	 * @param pair
	 */
	protected void consume(InOutPair pair) {
	}

	public void reset() {
	}

	/**
	 * Inform about an unsuccessful sampling attempt
	 * 
	 * @param nextInput
	 */
	public abstract void informFailure(double nextInput);

	/**
	 * This method ought to be called once you're done with this object (close remote connections etc.)
	 */
	public abstract void destroy();
}

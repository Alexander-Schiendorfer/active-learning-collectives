package de.uniaugsburg.isse.abstraction.selectors;

import java.util.List;

import de.uniaugsburg.isse.abstraction.InOutPair;

public class EquidistantSelector extends SamplingPointSelector {

	private List<Double> completePoints;
	private int currentIndex;

	public EquidistantSelector(List<Double> completePoints) {
		this.completePoints = completePoints;
		currentIndex = 0;
	}

	public List<Double> getCompletePoints() {
		return completePoints;
	}

	public void setCompletePoints(List<Double> completePoints) {
		this.completePoints = completePoints;
	}

	@Override
	public boolean hasNext() {
		boolean found = false;
		while (!found && currentIndex < completePoints.size()) {
			double nextInp = completePoints.get(currentIndex);

			// equality is only tested on the input!
			if (!sampledPoints.contains(new InOutPair(nextInp, 0.0))) {
				found = true;
			} else
				++currentIndex;
		}
		return currentIndex < completePoints.size();
	}

	/**
	 * Expects that 0 <= currentIndex < completePoints.size()
	 */
	@Override
	public double getNextInput() {
		double nextInput = completePoints.get(currentIndex);
		++currentIndex;
		return nextInput;
	}

	@Override
	public void informFailure(double nextInput) {
		// nothing to do
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}

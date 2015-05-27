package de.uniaugsburg.isse.abstraction.selectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import net.razorvine.pickle.PickleException;
import net.razorvine.pyro.NameServerProxy;
import net.razorvine.pyro.PyroException;
import net.razorvine.pyro.PyroProxy;
import de.uniaugsburg.isse.abstraction.InOutPair;
import de.uniaugsburg.isse.abstraction.types.Interval;

/**
 * This class implements an interface to the python based active learning implementation
 * 
 * @author alexander
 *
 */
public class PythonActiveLearningSelector extends SamplingPointSelector {

	public static NameServerProxy ns = null;
	private PyroProxy proxy;
	private boolean debug = true;

	private int noSamplingPoints;

	static {
		try {
			ns = NameServerProxy.locateNS("localhost");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public PythonActiveLearningSelector(int noSamplingPoints) {

		this.noSamplingPoints = noSamplingPoints;

		if (ns != null) {
			try {
				proxy = new PyroProxy(ns.lookup("isse.activelearningselector"));
				// File f = FileUtil.writeDoubleList(completeInputPoints);
				printCall("reset");
				proxy.call("reset", noSamplingPoints);

			} catch (PickleException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void printCall(String string) {
		if (debug)
			System.out.println("Trying call to " + string);

	}

	@Override
	protected void consume(InOutPair pair) {
		try {
			printCall("inform");
			proxy.call("inform", pair.getInput(), pair.getOutput());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void consumeInitialPoints(Collection<InOutPair> sampledPoints) {
		try {
			List<Double> inputs = new ArrayList<Double>(sampledPoints.size());
			List<Double> outputs = new ArrayList<Double>(sampledPoints.size());
			for (InOutPair ioPair : sampledPoints) {
				inputs.add(ioPair.getInput());
				outputs.add(ioPair.getOutput());
			}
			printCall("setInitialPoints");
			proxy.call("setInitialPoints", inputs, outputs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This needs to inform the proxy about feasible regions
	 */
	@Override
	public void setAbstractionData(SortedSet<Interval<Double>> generalFeasibleRegions, Collection<Interval<Double>> generalHoles) {
		super.setAbstractionData(generalFeasibleRegions, generalHoles);
		try {
			List<Double> feasLowers = new ArrayList<Double>(generalFeasibleRegions.size());
			List<Double> feasUppers = new ArrayList<Double>(generalFeasibleRegions.size());
			for (Interval<Double> feasReg : generalFeasibleRegions) {
				feasLowers.add(feasReg.min);
				feasUppers.add(feasReg.max);
			}
			printCall("setFeasibleRegions");
			proxy.call("setFeasibleRegions", feasLowers, feasUppers);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() {
		try {
			printCall("hasNextInput");
			return (Boolean) proxy.call("hasNextInput");
		} catch (PickleException e) {
			e.printStackTrace();
		} catch (PyroException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public double getNextInput() {
		try {

			printCall("getNextInput");
			Object val = proxy.call("getNextInput");
			return (Double) val;
		} catch (PickleException e) {
			e.printStackTrace();
		} catch (PyroException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0.0;
	}

	@Override
	public void reset() {
		try {
			printCall("reset " + noSamplingPoints);
			proxy.call("reset", noSamplingPoints);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void informFailure(double nextInput) {
		// TODO Auto-generated method stub informFailure
		try {
			printCall("informFailure");
			proxy.call("informFailure", nextInput);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		proxy.close();
	}
}

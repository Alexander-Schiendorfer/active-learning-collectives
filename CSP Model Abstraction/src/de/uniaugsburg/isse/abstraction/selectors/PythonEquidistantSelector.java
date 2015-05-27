package de.uniaugsburg.isse.abstraction.selectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.razorvine.pickle.PickleException;
import net.razorvine.pyro.NameServerProxy;
import net.razorvine.pyro.PyroException;
import net.razorvine.pyro.PyroProxy;
import de.uniaugsburg.isse.abstraction.InOutPair;

/**
 * This class implements precisely the same functionality as the equidistant java selector but implements csv file
 * handling to offer python interface
 * 
 * @author alexander
 *
 */
public class PythonEquidistantSelector extends SamplingPointSelector {

	public static NameServerProxy ns = null;

	private PyroProxy proxy;

	static {
		try {
			ns = NameServerProxy.locateNS("localhost");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public PythonEquidistantSelector(List<Double> completeInputPoints) {

		if (ns != null) {
			try {
				proxy = new PyroProxy(ns.lookup("isse.equidistantselector"));
				// File f = FileUtil.writeDoubleList(completeInputPoints);
				proxy.call("reset");
				proxy.call("setCompletePoints", completeInputPoints);

			} catch (PickleException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void consume(InOutPair pair) {
		try {
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
			proxy.call("setInitialPoints", inputs, outputs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() {
		try {
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
			proxy.call("reset");
		} catch (Exception e) {
			e.printStackTrace();
		}
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

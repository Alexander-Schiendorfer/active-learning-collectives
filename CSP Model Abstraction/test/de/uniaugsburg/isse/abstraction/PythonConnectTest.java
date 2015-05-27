package de.uniaugsburg.isse.abstraction;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.razorvine.pyro.NameServerProxy;
import net.razorvine.pyro.PyroProxy;

import org.junit.Test;

public class PythonConnectTest {

	@Test
	public void test() throws IOException {

		NameServerProxy ns = NameServerProxy.locateNS("localhost");
		PyroProxy remoteObject = new PyroProxy(ns.lookup("isse.expert"));

		Object result = remoteObject.call("getAnswer");
		Integer res = (Integer) result;
		System.out.println("The answer is " + res);

		// now send a pair
		InOutPair pair = new InOutPair(42.0, 30.0);
		Double resultd = (Double) remoteObject.call("sendPair", pair.getInput(), pair.getOutput());
		System.out.println("Result is " + resultd);

		List<Double> inputs = Arrays.asList(12.0, 45.0, 65.3);
		List<Double> outputs = Arrays.asList(24.0, 33.0, 27.3);

		resultd = (Double) remoteObject.call("sendPairs", inputs, outputs);
		System.out.println("Result is " + resultd);
		remoteObject.close();

	}
}

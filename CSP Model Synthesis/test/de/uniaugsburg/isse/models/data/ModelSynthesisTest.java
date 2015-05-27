package de.uniaugsburg.isse.models.data;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.uniaugsburg.isse.models.ModelSynthesiser;
import de.uniaugsburg.isse.models.data.naive.NaiveOPLOrganisationalTemplateFactory;
import de.uniaugsburg.isse.syntax.CplexModelSyntaxProvider;

public class ModelSynthesisTest {
	private final String organisationalTemplateFile = "Example.mod";

	@Test
	public void test() {
		ModelSynthesiser ms = new ModelSynthesiser();
		ms.setModelSyntaxProvider(new CplexModelSyntaxProvider());
		ms.setFactory(new NaiveOPLOrganisationalTemplateFactory(new File(organisationalTemplateFile)));
		List<String> individualAgentModels = Arrays.asList("a.mod", "b.mod", "c.mod");

		HashMap<String, String> initsA = new HashMap<String, String>();
		initsA.put("production", "55");
		HashMap<String, String> initsB = new HashMap<String, String>();
		initsB.put("production", "17");
		initsB.put("consStopping", "0");
		initsB.put("consRunning", "4");

		HashMap<String, String> initsC = new HashMap<String, String>();
		initsC.put("production", "0");
		initsC.put("consStopping", "5");
		initsC.put("consRunning", "0");
		initsC.put("countdown", "0");
		initsC.put("powerPlantState", "0");
		initsC.put("signal", "0");

		// all initial states
		Map<String, Map<String, String>> initStates = new HashMap<String, Map<String, String>>();
		initStates.put("a", initsA);
		initStates.put("b", initsB);
		initStates.put("c", initsC);
		ms.setInitialValues(initStates);
		System.out.println(ms.synthesise(organisationalTemplateFile, individualAgentModels));
	}

}

package de.uniaugsburg.isse.models.data;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.uniaugsburg.isse.models.data.naive.CplexIdentifierCrawler;

/**
 * Tests if all identifiers are correctly extracted from OTs and IAMs
 * 
 * @author alexander
 * 
 */
public class IdentifierSetTest {

	@Test
	public void testOrganisationalTemplate() {
		File file = new File("UnitCommitmentSimple.mod");
		CplexIdentifierCrawler ic = new CplexIdentifierCrawler();

		IdentifierSet identSet = ic.readIdentifiers(file);

		List<String> expecteds = Arrays.asList("feasibleRegions", "plants", "softConstraints", "LAST_SIMULATION_STEP", "TIMERANGE", "loadCurve", "production",
				"penalties", "totalProduction");
		Assert.assertEquals(expecteds.size(), identSet.getAllIdentifiers().size());

		for (String expectedIdentString : expecteds) {
			Assert.assertTrue(identSet.getAllIdentifiers().contains(expectedIdentString));
		}

		// variables
		List<String> variables = identSet.getVariables();
		Assert.assertEquals(6, variables.size());

		for (String expectedVar : Arrays.asList("plants", "feasibleRegions", "softConstraints", "LAST_SIMULATION_STEP", "TIMERANGE", "loadCurve")) {
			Assert.assertTrue(identSet.getVariables().contains(expectedVar));
		}

		// decision variables
		List<String> decVars = Arrays.asList("production", "penalties");
		Assert.assertEquals(decVars.size(), identSet.getDecisionVariables().size());
		for (String expectedIdentString : decVars) {
			Assert.assertTrue(identSet.getDecisionVariables().contains(expectedIdentString));
		}

		// decision expressions
		List<String> decExprs = Arrays.asList("totalProduction");
		Assert.assertEquals(decExprs.size(), identSet.getDecisionExpressions().size());
		for (String expectedIdentString : decExprs) {
			Assert.assertTrue(identSet.getDecisionExpressions().contains(expectedIdentString));
		}

	}

	@Test
	public void LineSplitterTest() {
		LineSplitter ls = new LineSplitter();
		List<String> strings = ls.readLines(new File("UnitCommitmentSimple.mod"));
		for (String s : strings) {
			System.out.println(s);
		}
	}
}

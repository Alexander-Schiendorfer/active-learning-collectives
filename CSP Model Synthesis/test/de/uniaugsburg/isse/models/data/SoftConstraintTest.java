package de.uniaugsburg.isse.models.data;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.uniaugsburg.isse.models.ConstraintSatisfactionProblem;
import de.uniaugsburg.isse.models.data.naive.NaiveOPLOrganisationalTemplateFactory;

public class SoftConstraintTest {

	@Test
	public void test() {
		File f = new File("UnitCommitmentSimpleSoftConstraints.mod");
		NaiveOPLOrganisationalTemplateFactory fact = new NaiveOPLOrganisationalTemplateFactory(f);
		ConstraintSatisfactionProblem ot = fact.create();
		List<String> softConstraints = ot.getConstraints().getSoftConstraints();
		List<String> constraintRelationships = ot.getConstraints().getConstraintRelationships();
		Assert.assertEquals(3, softConstraints.size());
		Assert.assertEquals(3, constraintRelationships.size());

		List<String> expectedSoftConstraints = Arrays.asList("oc1", "oc2", "oc3");
		for (String s : expectedSoftConstraints) {
			Assert.assertTrue(softConstraints.contains(s));
		}
	}
}

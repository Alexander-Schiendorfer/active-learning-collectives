package utilities.test;

import java.util.Arrays;

import org.junit.Test;

import utilities.Randomizer;

public class RandomizerTest {

	@Test
	public void GaussReturnsProb() {
		System.out.println(Arrays.toString(Randomizer.getInstance().calculateAlphaBeta(0.02, Math.pow(0.005, 2))));

		// int min = 0;
		// int max = 1;
		//
		// for (; max < 10; max++)
		// System.out.println(Randomizer.getInstance().createRandomNumber(min, max));
	}
}
